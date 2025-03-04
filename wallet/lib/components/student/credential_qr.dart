import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:bac_web3/components/verifier/model/verifiable_credential.dart';

class CredentialQRPage extends StatelessWidget {
  final VerifiableCredential credential;

  const CredentialQRPage({
    super.key,
    required this.credential,
  });

  @override
  Widget build(BuildContext context) {
    // Convert the credential to a JSON string
    final credentialJson = jsonEncode(credential.toJson());

    return Scaffold(
      appBar: AppBar(
        title: const Text('Credential QR Code'),
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Card(
                elevation: 4,
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: [
                      Text(
                        credential.type,
                        style: Theme.of(context).textTheme.headlineSmall,
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 24),
                      QrImageView(
                        data: credentialJson,
                        version: QrVersions.auto,
                        size: 280.0,
                        backgroundColor: Colors.white,
                      ),
                      const SizedBox(height: 24),
                      Text(
                        'Scan this QR code to verify the credential',
                        style: Theme.of(context).textTheme.bodyLarge,
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
