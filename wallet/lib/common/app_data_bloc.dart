import 'dart:convert';

import 'package:bac_web3/components/diploma/model/uploaded_diploma.dart';
import 'package:bac_web3/components/verifier/model/verifiable_credential.dart';
import 'package:bac_web3/components/verifier/verifier_service.dart';
import 'package:bip39/bip39.dart' as bip39;
import 'package:bac_web3/common/diploma.dart';
import 'package:convert/convert.dart';
import 'package:ed25519_hd_key/ed25519_hd_key.dart';
import 'package:equatable/equatable.dart';
import 'package:fast_rsa/fast_rsa.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:hex/hex.dart';
import 'package:logging/logging.dart';
import 'package:web3dart/credentials.dart';
import 'package:web3dart/crypto.dart';

class AppDataState extends Equatable {
  final KeyPair keyPair;
  final Diploma bacDiploma;
  final List<UploadedDiploma> uploadedDiplomaList;
  final bool appliedToFaculty;
  final List<VerifiableCredential> verifiableCredentials;

  const AppDataState(
    this.keyPair,
    this.bacDiploma,
    this.appliedToFaculty,
    this.uploadedDiplomaList,
    this.verifiableCredentials,
  );

  @override
  List<Object> get props => [
        keyPair,
        bacDiploma,
        appliedToFaculty,
        uploadedDiplomaList,
        verifiableCredentials,
      ];

  AppDataState copyWith({
    KeyPair? keyPair,
    Diploma? bacDiploma,
    bool? appliedToFaculty,
    List<UploadedDiploma>? uploadedDiplomaList,
    List<VerifiableCredential>? verifiableCredentials,
  }) {
    return AppDataState(
      keyPair ?? this.keyPair,
      bacDiploma ?? this.bacDiploma,
      appliedToFaculty ?? this.appliedToFaculty,
      uploadedDiplomaList ?? this.uploadedDiplomaList,
      verifiableCredentials ?? this.verifiableCredentials,
    );
  }

  get hasValidKeys => keyPair.publicKey.isNotEmpty && keyPair.privateKey.isNotEmpty;

  get publicKey => keyPair.publicKey;

  get privateKey => keyPair.privateKey;
}

class AppDataInitial extends AppDataState {
  AppDataInitial() : super(KeyPair('', ''), Diploma.empty(), false, [], []);
}

class AppDataBloc extends Cubit<AppDataState> {
  final VerifierService verifierService = VerifierService();
  final log = Logger('AppDataBloc');
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  static const String _credentialsKey = 'verifiable_credentials';

  AppDataBloc() : super(AppDataInitial()) {
    _init();
  }

  Future<void> _init() async {
    await loadVerifiableCredentials();
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
    final master = await ED25519_HD_KEY.getMasterKeyFromSeed(hex.decode(seed), masterSecret: 'Bitcoin seed');
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

  Future<void> addVerifiableCredential(VerifiableCredential credential) async {
    final currentCredentials = List<VerifiableCredential>.from(state.verifiableCredentials);
    log.info('Current credentials count: ${currentCredentials.length}');

    // Check if credential already exists
    if (!currentCredentials.any((c) => c.id == credential.id)) {
      currentCredentials.add(credential);
      log.info('Adding new credential with ID: ${credential.id}');

      // Save to secure storage
      final credentialsJson = jsonEncode(
        currentCredentials.map((c) => c.toJson()).toList(),
      );
      await _secureStorage.write(key: _credentialsKey, value: credentialsJson);
      log.info('Saved credentials to secure storage');

      // Update state
      emit(state.copyWith(verifiableCredentials: currentCredentials));
      log.info('Updated state with new credentials count: ${currentCredentials.length}');
    } else {
      log.info('Credential with ID: ${credential.id} already exists');
    }
  }

  Future<void> loadVerifiableCredentials() async {
    log.info('Loading verifiable credentials from secure storage');
    final credentialsJson = await _secureStorage.read(key: _credentialsKey);
    if (credentialsJson != null) {
      log.info('Found credentials in secure storage');
      try {
        final List<dynamic> jsonList = jsonDecode(credentialsJson);
        final credentials = jsonList.map((json) => VerifiableCredential.fromJson(json)).toList();
        log.info('Loaded ${credentials.length} credentials');
        emit(state.copyWith(verifiableCredentials: credentials));
      } catch (e, stackTrace) {
        log.severe('Error loading credentials: $e');
        log.severe('Stack trace: $stackTrace');
      }
    } else {
      log.info('No credentials found in secure storage');
    }
  }

  Future<void> removeVerifiableCredential(String credentialId) async {
    final currentCredentials = List<VerifiableCredential>.from(state.verifiableCredentials);
    log.info('Removing credential with ID: $credentialId');
    currentCredentials.removeWhere((c) => c.id == credentialId);

    // Save to secure storage
    final credentialsJson = jsonEncode(
      currentCredentials.map((c) => c.toJson()).toList(),
    );
    await _secureStorage.write(key: _credentialsKey, value: credentialsJson);
    log.info('Saved updated credentials to secure storage');

    // Update state
    emit(state.copyWith(verifiableCredentials: currentCredentials));
    log.info('Updated state with new credentials count: ${currentCredentials.length}');
  }

  void addVerifiableCredentialFromQRCode(String qrCodeContents) {
    log.info('Attempting to add credential from QR code');
    try {
      final Map<String, dynamic> json = jsonDecode(qrCodeContents);
      log.info('Successfully parsed QR code contents');
      final credential = VerifiableCredential.fromJson(json);
      log.info('Created credential object with ID: ${credential.id}');
      addVerifiableCredential(credential);
    } catch (e, stackTrace) {
      log.severe('Error adding credential from QR code: $e');
      log.severe('Stack trace: $stackTrace');
      rethrow;
    }
  }

  void addVerifiableCredentialFromJson(String jsonString) {
    log.info('Attempting to add credential from JSON');
    try {
      final Map<String, dynamic> json = jsonDecode(jsonString);
      log.info('Successfully parsed JSON');
      final credential = VerifiableCredential.fromJson(json);
      log.info('Created credential object with ID: ${credential.id}');
      addVerifiableCredential(credential);
    } catch (e, stackTrace) {
      log.severe('Error adding credential from JSON: $e');
      log.severe('Stack trace: $stackTrace');
      rethrow;
    }
  }
}
