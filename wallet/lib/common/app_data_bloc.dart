import 'dart:convert';

import 'package:bac_web3/components/auth/auth_service.dart';
import 'package:bac_web3/components/diploma/model/uploaded_diploma.dart';
import 'package:bac_web3/components/verifier/verifier_service.dart';
import 'package:bip39/bip39.dart' as bip39;
import 'package:http/http.dart' as http;
import 'package:bac_web3/common/diploma.dart';
import 'package:convert/convert.dart';
import 'package:ed25519_hd_key/ed25519_hd_key.dart';
import 'package:equatable/equatable.dart';
import 'package:fast_rsa/fast_rsa.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:bac_web3/common/constants.dart';
import 'package:hex/hex.dart';
import 'package:logging/logging.dart';
import 'package:web3dart/credentials.dart';
import 'package:web3dart/crypto.dart';

class AppDataState extends Equatable {
  final KeyPair keyPair;
  final Diploma bacDiploma;
  final List<UploadedDiploma> uploadedDiplomaList;
  final bool appliedToFaculty;

  const AppDataState(this.keyPair, this.bacDiploma, this.appliedToFaculty,
      this.uploadedDiplomaList);

  @override
  List<Object> get props =>
      [keyPair, bacDiploma, appliedToFaculty, uploadedDiplomaList];

  AppDataState copyWith(
      {KeyPair? keyPair,
      Diploma? bacDiploma,
      bool? appliedToFaculty,
      List<UploadedDiploma>? uploadedDiplomaList}) {
    return AppDataState(
      keyPair ?? this.keyPair,
      bacDiploma ?? this.bacDiploma,
      appliedToFaculty ?? this.appliedToFaculty,
      uploadedDiplomaList ?? this.uploadedDiplomaList,
    );
  }

  get hasValidKeys =>
      keyPair.publicKey.isNotEmpty && keyPair.privateKey.isNotEmpty;

  get publicKey => keyPair.publicKey;

  get privateKey => keyPair.privateKey;
}

class AppDataInitial extends AppDataState {
  AppDataInitial() : super(KeyPair('', ''), Diploma.empty(), false, []);
}

class AppDataBloc extends Cubit<AppDataState> {
  final VerifierService verifierService = VerifierService();
  final log = Logger('AppDataBloc');
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  // the keys for secure shared preferences which will
  // hold the public and private keys for signing
  static const String _publicKeyKey = 'publicKey';
  static const String _privateKeyKey = 'privateKey';

  AppDataBloc() : super(AppDataInitial()) {
    _init();
  }

  Future<void> _init() async {
    String? publicKey = await _secureStorage.read(key: _publicKeyKey);
    String? privateKey = await _secureStorage.read(key: _privateKeyKey);

    if (publicKey != null && privateKey != null) {
      log.info('Keys found in secure storage');
      emit(state.copyWith(
        keyPair: KeyPair(publicKey, privateKey),
      ));
      print('Public key: ${convertStringToHex(state.keyPair.publicKey)}');
    } else {
      log.info('Keys not found in secure storage, generating new keys');
      KeyPair keyPair = await RSA.generate(RSA_KEY_LENGTH);
      await _saveKeys(keyPair);
      emit(state.copyWith(keyPair: keyPair));
    }
    getUploadedDocuments();
  }

  Future<void> _saveKeys(KeyPair keyPair) async {
    // convert to hexadecimal the keyPair.publicKey

    print('Public key: ${convertStringToHex(keyPair.publicKey)}');

    log.info('Saving keys to secure storage');
    await _secureStorage.write(key: _publicKeyKey, value: keyPair.publicKey);
    await _secureStorage.write(key: _privateKeyKey, value: keyPair.privateKey);
  }

  void setBacDiplomaFromQRCode(String qrCodeContents) {
    setBacDiploma(Diploma.fromJson(jsonDecode(qrCodeContents)));
  }

  void setBacDiploma(Diploma diploma) {
    emit(state.copyWith(bacDiploma: diploma));
  }

  void setAppliedToFaculty(bool value) {
    emit(state.copyWith(appliedToFaculty: value));
  }

  String convertStringToHex(String data) {
    String hex = '';
    for (var i = 0; i < data.length; i++) {
      hex += data.codeUnitAt(i).toRadixString(16);
    }
    return hex;
  }

  Future<String> sign(String data) {
    if (!state.hasValidKeys) {
      throw Exception('Keys are not valid');
    }
    return RSA.signPKCS1v15(data, Hash.SHA256, state.keyPair.privateKey);
  }

  Future<void> setMnemonic(String? value) async {
    await _secureStorage.write(key: 'mnemonic', value: value ?? '');
  }

  Future<void> setPrivateKey(String? value) async {
    await _secureStorage.write(key: 'privateKey', value: value ?? '');
  }

  String generateMnemonic() {
    return bip39.generateMnemonic();
  }

  String entropyToMnemonic(String entropyMnemonic) {
    return bip39.entropyToMnemonic(entropyMnemonic);
  }

  Future<String> getPrivateKey(String mnemonic) async {
    final seed = bip39.mnemonicToSeedHex(mnemonic);
    final master = await ED25519_HD_KEY.getMasterKeyFromSeed(hex.decode(seed),
        masterSecret: 'Bitcoin seed');
    final privateKey = HEX.encode(master.key);

    print('private: $privateKey');

    return privateKey;
  }

  String toChecksumAddress(String address) {
    address = address.replaceAll('0x', '');
    String lowerAddress = address.toLowerCase();
    String hash = bytesToHex(keccak256(utf8.encode(lowerAddress)));

    String checksumAddress = '0x';
    for (int i = 0; i < lowerAddress.length; i++) {
      if (int.parse(hash[i], radix: 16) > 7) {
        checksumAddress += lowerAddress[i].toUpperCase();
      } else {
        checksumAddress += lowerAddress[i];
      }
    }

    return checksumAddress;
  }

  String bytesToHex(List<int> bytes) {
    return bytes.map((byte) => byte.toRadixString(16).padLeft(2, '0')).join('');
  }

  Future<String> getPublicAddressWithChecksum(String privateKey) async {
    final private = EthPrivateKey.fromHex(privateKey);

    print('address: ${private.address.hex}');

    String checksumAddress = toChecksumAddress(private.address.hex);
    print('checksum address: $checksumAddress');

    return checksumAddress;
  }

  Future<EthereumAddress> getPublicAddress(String privateKey) async {
    final private = EthPrivateKey.fromHex(privateKey);

    print('address: ${private.address}');

    return private.address;
  }

  Future<bool> setupFromMnemonic(String mnemonic) async {
    final cryptMnemonic = bip39.mnemonicToEntropy(mnemonic);
    final privateKey = await getPrivateKey(mnemonic);

    // await _configService.setMnemonic(cryptMnemonic);
    // await _configService.setPrivateKey(privateKey);
    return true;
  }

  Future<bool> setupFromPrivateKey(String privateKey) async {
    // await _configService.setMnemonic(null);
    // await _configService.setPrivateKey(privateKey);
    return true;
  }

  // Future<bool> confirmMnemonic(String mnemonic) async {
  //   if (state.mnemonic != mnemonic) {
  //     _store
  //         .dispatch(WalletSetupAddError('Invalid mnemonic, please try again.'));
  //     return false;
  //   }
  //   _store.dispatch(WalletSetupStarted());
  //
  //   await _addressService.setupFromMnemonic(mnemonic);
  //
  //   return true;
  // }
  //
  // Future<bool> importFromMnemonic(String mnemonic) async {
  //   try {
  //     _store.dispatch(WalletSetupStarted());
  //
  //     if (_validateMnemonic(mnemonic)) {
  //       final normalisedMnemonic = _mnemonicNormalise(mnemonic);
  //       await _addressService.setupFromMnemonic(normalisedMnemonic);
  //       return true;
  //     }
  //   } catch (e) {
  //     _store.dispatch(WalletSetupAddError(e.toString()));
  //   }
  //
  //   _store.dispatch(
  //       WalletSetupAddError('Invalid mnemonic, it requires 12 words.'));
  //
  //   return false;
  // }
  //
  // Future<bool> importFromPrivateKey(String privateKey) async {
  //   try {
  //     _store.dispatch(WalletSetupStarted());
  //
  //     await _addressService.setupFromPrivateKey(privateKey);
  //     return true;
  //   } catch (e) {
  //     _store.dispatch(WalletSetupAddError(e.toString()));
  //   }
  //
  //   _store.dispatch(
  //       WalletSetupAddError('Invalid private key, please try again.'));
  //
  //   return false;
  // }

  String _mnemonicNormalise(String mnemonic) {
    return _mnemonicWords(mnemonic).join(' ');
  }

  List<String> _mnemonicWords(String mnemonic) {
    return mnemonic
        .split(' ')
        .where((item) => item.trim().isNotEmpty)
        .map((item) => item.trim())
        .toList();
  }

  bool _validateMnemonic(String mnemonic) {
    return _mnemonicWords(mnemonic).length == 12;
  }

  void uploadFile() {
    final url = Uri.https(
        BACKEND_URL, '$BACKEND_API_PREFIX/documents/upload-document/');
    http.post(url, body: {'name': 'doodle', 'file': 'file'});
  }

  void signBacDiploma(UploadedDiploma uploadedDiploma) {
    log.info(
        'Signing document ${uploadedDiploma.id} and user ${AuthService.instance.authIdToken?.sub}');
    final url = Uri.https(
        BACKEND_URL, '$BACKEND_API_PREFIX/credential-manager/sign-document/');
    http
        .post(url,
            headers: {
              'Authorization': 'Bearer ${AuthService.instance.accessToken}',
              'Content-Type': 'application/json'
            },
            body: jsonEncode({
              'document_id': uploadedDiploma.id.toString(),
              'recipient_public_address':
                  AuthService.instance.authIdToken?.sub ?? '',
            }))
        .then((response) {
      if (response.statusCode == 200) {
        log.info('Document signed successfully');
        getUploadedDocuments();
      } else {
        log.warning('Failed to sign document: ${response.statusCode}');
      }
    }).catchError((error) {
      log.warning('Error signing document: $error');
    });
  }

  void uploadSignedDocument(String destinationUrl) {
    // TODO WILL UPLOAD THE SIGNED DIPLOMA
  }

  void getUploadedDocuments() {
    if ((AuthService.instance.authIdToken?.sub ?? '') == '') {
      log.warning('No user ID found, retrying in 500ms');
      Future.delayed(
          const Duration(milliseconds: 500), () => getUploadedDocuments());
      return;
    }
    final url = Uri.parse(
        '$BACKEND_URL$BACKEND_API_PREFIX/documents/my/?user_public_key=${AuthService.instance.authIdToken?.sub}');

    http.get(url).then((response) {
      if (response.statusCode == 200) {
        var documents = jsonDecode(response.body);
        final List<UploadedDiploma> uploadedDiplomas = documents['documents']
            .map<UploadedDiploma>(
                (document) => UploadedDiploma.fromJson(document))
            .toList();
        emit(state.copyWith(uploadedDiplomaList: uploadedDiplomas));
        log.info('Got ${uploadedDiplomas.length} uploaded documents.');
      } else {
        log.warning('Failed to get uploaded documents: ${response.statusCode}');
      }
    }).catchError((error) {
      log.warning('Error getting uploaded documents: $error');
    });
  }
}
