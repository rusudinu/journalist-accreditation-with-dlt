import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:bac_web3/components/student/add_credential.dart';
import 'package:bac_web3/components/student/present_credential.dart';
import 'package:bac_web3/components/verifier/manage_credentials.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class StudentPage extends StatelessWidget {
  const StudentPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('User Dashboard'),
      ),
      body: BlocBuilder<AppDataBloc, AppDataState>(
        builder: (context, state) {
          return Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const AddDiplomaToWallet(),
                      ),
                    );
                  },
                  child: const Text('Add Document'),
                ),
                const SizedBox(height: 16),
                ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const PresentDiplomaByScanning(),
                      ),
                    );
                  },
                  child: const Text('Present Document'),
                ),
                const SizedBox(height: 16),
                ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const ManageCredentials(),
                      ),
                    );
                  },
                  child: const Text('Manage Credentials'),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
