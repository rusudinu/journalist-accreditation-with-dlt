import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:bac_web3/components/verifier/model/verifiable_credential.dart';
import 'package:bac_web3/components/verifier/scan_credential.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class ManageCredentials extends StatefulWidget {
  const ManageCredentials({super.key});

  @override
  State<ManageCredentials> createState() => _ManageCredentialsState();
}

class _ManageCredentialsState extends State<ManageCredentials> {
  final TextEditingController _jsonController = TextEditingController();
  bool _isAddingJson = false;

  @override
  void dispose() {
    _jsonController.dispose();
    super.dispose();
  }

  void _scanQRCode() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const ScanCredential()),
    );
  }

  void _toggleJsonInput() {
    setState(() {
      _isAddingJson = !_isAddingJson;
      if (!_isAddingJson) {
        _jsonController.clear();
      }
    });
  }

  void _addJsonCredential() {
    try {
      context.read<AppDataBloc>().addVerifiableCredentialFromJson(_jsonController.text);
      _jsonController.clear();
      setState(() {
        _isAddingJson = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Credential added successfully'),
          backgroundColor: Colors.green,
        ),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Failed to add credential: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Manage Credentials'),
      ),
      body: BlocBuilder<AppDataBloc, AppDataState>(
        builder: (context, state) {
          return Column(
            children: [
              Expanded(
                child: state.verifiableCredentials.isEmpty
                    ? const Center(
                        child: Text('No credentials stored'),
                      )
                    : ListView.builder(
                        itemCount: state.verifiableCredentials.length,
                        itemBuilder: (context, index) {
                          final credential = state.verifiableCredentials[index];
                          return Card(
                            margin: const EdgeInsets.all(8.0),
                            child: ListTile(
                              title: Text('Credential ID: ${credential.id}'),
                              subtitle: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text('Issuer: ${credential.issuer}'),
                                  Text('Status: ${credential.credentialSubject.status}'),
                                  Text('Issued: ${credential.issuanceDate}'),
                                ],
                              ),
                              trailing: IconButton(
                                icon: const Icon(Icons.delete),
                                onPressed: () {
                                  context.read<AppDataBloc>().removeVerifiableCredential(credential.id);
                                },
                              ),
                            ),
                          );
                        },
                      ),
              ),
              if (_isAddingJson)
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: [
                      TextField(
                        controller: _jsonController,
                        decoration: const InputDecoration(
                          labelText: 'Paste credential JSON',
                          border: OutlineInputBorder(),
                        ),
                        maxLines: 4,
                      ),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          ElevatedButton(
                            onPressed: _addJsonCredential,
                            child: const Text('Add Credential'),
                          ),
                          TextButton(
                            onPressed: _toggleJsonInput,
                            child: const Text('Cancel'),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton.icon(
                      onPressed: _scanQRCode,
                      icon: const Icon(Icons.qr_code_scanner),
                      label: const Text('Scan QR Code'),
                    ),
                    if (!_isAddingJson)
                      ElevatedButton.icon(
                        onPressed: _toggleJsonInput,
                        icon: const Icon(Icons.paste),
                        label: const Text('Paste JSON'),
                      ),
                  ],
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
