import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:bac_web3/components/student/add_credential.dart';
import 'package:bac_web3/components/student/present_credential.dart';
import 'package:bac_web3/components/student/credential_qr.dart';
import 'package:bac_web3/components/verifier/manage_credentials.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class StudentPage extends StatelessWidget {
  const StudentPage({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AppDataBloc, AppDataState>(
      builder: (context, state) {
        return SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: <Widget>[
                // Credentials List Section
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'Your Credentials',
                              style: TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            Text(
                              '${state.verifiableCredentials.length} stored',
                              style: const TextStyle(
                                color: Colors.grey,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        state.verifiableCredentials.isEmpty
                            ? const Padding(
                                padding: EdgeInsets.symmetric(vertical: 16.0),
                                child: Text(
                                  'No credentials stored yet',
                                  style: TextStyle(
                                    color: Colors.grey,
                                    fontStyle: FontStyle.italic,
                                  ),
                                ),
                              )
                            : ConstrainedBox(
                                constraints: const BoxConstraints(maxHeight: 300),
                                child: ListView.builder(
                                  shrinkWrap: true,
                                  physics: const AlwaysScrollableScrollPhysics(),
                                  itemCount: state.verifiableCredentials.length,
                                  itemBuilder: (context, index) {
                                    final credential = state.verifiableCredentials[index];
                                    return Card(
                                      margin: const EdgeInsets.symmetric(vertical: 4),
                                      child: InkWell(
                                        onTap: () {
                                          Navigator.push(
                                            context,
                                            MaterialPageRoute(
                                              builder: (context) => CredentialQRPage(
                                                credential: credential,
                                              ),
                                            ),
                                          );
                                        },
                                        child: ListTile(
                                          title: Text(
                                            credential.type,
                                            style: const TextStyle(fontWeight: FontWeight.bold),
                                          ),
                                          subtitle: Column(
                                            crossAxisAlignment: CrossAxisAlignment.start,
                                            children: [
                                              Text('Issued by: ${credential.issuer}'),
                                              Text('Date: ${credential.issuanceDate}'),
                                            ],
                                          ),
                                          trailing: Container(
                                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                            decoration: BoxDecoration(
                                              color: credential.credentialSubject.status.toLowerCase() == 'valid' ? Colors.green.withOpacity(0.1) : Colors.red.withOpacity(0.1),
                                              borderRadius: BorderRadius.circular(12),
                                            ),
                                            child: Text(
                                              credential.credentialSubject.status,
                                              style: TextStyle(
                                                color: credential.credentialSubject.status.toLowerCase() == 'valid' ? Colors.green : Colors.red,
                                                fontWeight: FontWeight.bold,
                                              ),
                                            ),
                                          ),
                                          isThreeLine: true,
                                        ),
                                      ),
                                    );
                                  },
                                ),
                              ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                // Actions Section
                const Text(
                  'Actions',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 16),
                ElevatedButton.icon(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const AddDiplomaToWallet(),
                      ),
                    );
                  },
                  icon: const Icon(Icons.add),
                  label: const Text('Add Document'),
                ),
                const SizedBox(height: 16),
                ElevatedButton.icon(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const PresentDiplomaByScanning(),
                      ),
                    );
                  },
                  icon: const Icon(Icons.present_to_all),
                  label: const Text('Present Document'),
                ),
                const SizedBox(height: 16),
                ElevatedButton.icon(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const ManageCredentials(),
                      ),
                    );
                  },
                  icon: const Icon(Icons.settings),
                  label: const Text('Manage Credentials'),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
