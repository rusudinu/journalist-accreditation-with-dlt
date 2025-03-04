import 'package:flutter_bloc/flutter_bloc.dart';
import 'dart:convert';
import 'dart:io';

import 'package:bac_web3/common/app_data_bloc.dart';

import 'package:bac_web3/common/components/section.dart';
import 'package:bac_web3/common/constants.dart';
import 'package:bac_web3/components/auth/auth_service.dart';
import 'package:bac_web3/components/wallet/wallet_create_page.dart';
import 'package:bac_web3/components/wallet/wallet_import_page.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:fast_rsa/fast_rsa.dart' as fastRsa;
import 'package:path/path.dart' as path;

class DevPage extends StatefulWidget {
  const DevPage({super.key});

  @override
  State<DevPage> createState() => _DevPageState();
}

class _DevPageState extends State<DevPage> {
  String accesstoken = "ns";

  void pickAFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();

    if (result != null) {
      final url = Uri.http(
          BACKEND_URL, '$BACKEND_API_PREFIX/documents/upload-document/');
      File file = File(result.files.single.path!);
      var request = http.MultipartRequest('POST', url);
      request.files.add(
        await http.MultipartFile.fromPath(
          'document',
          file.path,
          filename: path.basename(file.path),
        ),
      );
      request.fields['description'] =
          'Diploma for user ${AuthService.instance.authIdToken?.sub ?? ''}';
      request.fields['user_public_key'] =
          AuthService.instance.authIdToken?.sub ?? '';
      try {
        var response = await request.send();
        if (response.statusCode == 200) {
          print('Document uploaded successfully');
          var responseData = await response.stream.toBytes();
          var responseString = String.fromCharCodes(responseData);

          print(responseString);

          context.read<AppDataBloc>().getUploadedDocuments();
        } else {
          print('Failed to upload document: ${response.statusCode}');
          var responseData = await response.stream.toBytes();
          var responseString = String.fromCharCodes(responseData);
          print(responseString);
        }
      } catch (e) {
        print('Error uploading document: $e');
      }
    } else {
      // User canceled the picker
    }
  }

  void registerIssuer() async {
    final url = Uri.http(
        BACKEND_URL, '$BACKEND_API_PREFIX/credential-manager/register-issuer/');

    AuthService authService = AuthService.instance;

    setState(() {
      accesstoken = authService.accessToken ?? 'not auth';
    });

    final response = await http.post(url,
        headers: {
          'Authorization': 'Bearer ${authService.accessToken}',
          'Content-Type': 'application/json'
        },
        body: jsonEncode({
          "issuer_did": "politehnica-rusu-test",
          "public_key": "0x0000000000000000000000000000000000000000"
        }));

    print(response.statusCode);
    print(response.body);
  }

  void checkIssuer() async {
    String issuerDid = 'politehnica-rusu-test';
    final url = Uri.http(BACKEND_URL,
        '$BACKEND_API_PREFIX/credential-manager/get-issuer/$issuerDid');

    AuthService authService = AuthService.instance;

    setState(() {
      accesstoken = authService.accessToken ?? 'not auth';
    });

    final response = await http.get(
      url,
      headers: {
        'Authorization': 'Bearer ${authService.accessToken}',
      },
    );

    print(response.statusCode);
    print(response.body);
  }

  void copyTokenToClipboard() {
    Clipboard.setData(
        ClipboardData(text: AuthService.instance.accessToken ?? 'not auth'));
  }

  void copyUserIDToClipboard() {
    Clipboard.setData(ClipboardData(
        text: AuthService.instance.authIdToken?.sub ?? 'not auth'));
  }

  void encrypt() async {
    String payload = 'Hello world';

    fastRsa.KeyPair keyPair = await fastRsa.RSA.generate(RSA_KEY_LENGTH);
    print(keyPair.publicKey);
    print(keyPair.privateKey);

    await fastRsa.RSA
        .encryptOAEP(payload, '', fastRsa.Hash.SHA256, keyPair.publicKey)
        .then((value) {
      print(value);

      fastRsa.RSA
          .decryptOAEP(value, '', fastRsa.Hash.SHA256, keyPair.privateKey)
          .then((value) {
        print(value);
      });
    });
  }

  void generateBacDiploma() async {
    // context.read<AppDataBloc>().setBacDiplomaFromQRCode(jsonEncode({
    //       'serial': '523475',
    //       'name': 'Alexandru Georgescu',
    //       'cif': '123456',
    //       'cnp': '1234567890123',
    //       'session': 'Iunie 2020',
    //       'grade': '9.75',
    //     }));
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        children: [
          Section(
            sectionTitle: 'User data',
            sectionContent: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: generateBacDiploma,
                  child: const Text('Generate bac diploma'),
                ),
              ],
            ),
          ),
          Section(
            sectionTitle: 'Auth',
            sectionContent: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: () async {
                    final result = await AuthService.instance.login();

                    if (result != 'SUCCESS') {
                      final snackBar = SnackBar(
                        content: Text(result),
                      );

                      //ScaffoldMessenger.of(context).showSnackBar(snackBar);
                    }
                  },
                  child: const Text('Login'),
                ),
                ElevatedButton(
                  onPressed: () async {
                    final result = await AuthService.instance.logout();
                  },
                  child: const Text('Logout'),
                ),
              ],
            ),
          ),
          Section(
            sectionTitle: 'Issuer',
            sectionContent: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: checkIssuer,
                  child: const Text('Check issuer'),
                ),
                ElevatedButton(
                  onPressed: registerIssuer,
                  child: const Text('Register issuer'),
                ),
              ],
            ),
          ),
          Section(
            sectionTitle: 'Wallet',
            sectionContent: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (context) => const WalletCreatePage()),
                    );
                  },
                  child: const Text('Create wallet'),
                ),
                ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (context) =>
                              const WalletImportPage('Import wallet')),
                    );
                  },
                  child: const Text('Import wallet'),
                ),
              ],
            ),
          ),
          Section(
            sectionTitle: 'Other',
            sectionContent: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: encrypt,
                  child: const Text('Test encrypt'),
                ),
                ElevatedButton(
                  onPressed: pickAFile,
                  child: const Text('Pick a file'),
                ),
              ],
            ),
          ),
          Section(
            sectionTitle: 'Meta',
            sectionContent: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    IconButton(
                        onPressed: copyTokenToClipboard,
                        icon: const Icon(Icons.copy)),
                    Text(
                      (AuthService.instance.accessToken ?? 'not auth')
                          .substring(
                              0,
                              (AuthService.instance.accessToken ?? 'not auth')
                                          .length >
                                      15
                                  ? 15
                                  : 0),
                      softWrap: true,
                    )
                  ],
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    IconButton(
                        onPressed: copyUserIDToClipboard,
                        icon: const Icon(Icons.copy)),
                    Text(
                      (AuthService.instance.authIdToken?.sub ?? 'not auth')
                          .substring(
                              0,
                              (AuthService.instance.authIdToken?.sub ??
                                              'not auth')
                                          .length >
                                      15
                                  ? 15
                                  : 0),
                      softWrap: true,
                    )
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
