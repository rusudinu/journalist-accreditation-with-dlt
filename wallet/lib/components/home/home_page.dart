import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: BlocBuilder<AppDataBloc, AppDataState>(
        buildWhen: (previous, current) =>
            previous.uploadedDiplomaList != current.uploadedDiplomaList,
        builder: (context, state) {
          return ListView.builder(
              itemCount: state.uploadedDiplomaList.length,
              itemBuilder: (context, index) {
                return Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        ListTile(
                          leading:
                              const Icon(Icons.vpn_key, color: Colors.blue),
                          title: Text(
                              state.uploadedDiplomaList[index].description,
                              style: const TextStyle(
                                  fontSize: 18, fontWeight: FontWeight.bold)),
                        ),
                        const SizedBox(height: 10),
                        if (state.uploadedDiplomaList[index].canBeSigned())
                          const SizedBox(height: 10),
                        ListTile(
                          leading: state.uploadedDiplomaList[index]
                                  .canBeSigned()
                              ? const Icon(Icons.cancel, color: Colors.red)
                              : const Icon(Icons.check, color: Colors.green),
                          title: Text(
                              state.uploadedDiplomaList[index].canBeSigned()
                                  ? 'Nu a fost inca semnata'
                                  : 'Signature: ${state.uploadedDiplomaList[index].signature}',
                              style: const TextStyle(
                                  fontSize: 18, fontWeight: FontWeight.normal)),
                        ),
                        if (state.uploadedDiplomaList[index].canBeSigned())
                          ElevatedButton(
                            onPressed: () {
                              context.read<AppDataBloc>().signBacDiploma(
                                  state.uploadedDiplomaList[index]);
                            },
                            child: const Text('Sign diploma'),
                          ),
                      ],
                    ),
                  ),
                );
              });
          //   Center(
          //   child: Column(
          //     children: [
          //       if (state.bacDiploma.isEmpty())
          //         const Card(
          //           child: Padding(
          //             padding: EdgeInsets.all(16),
          //             child: ListTile(
          //               leading:
          //                   Icon(Icons.tips_and_updates, color: Colors.amber),
          //               title: Text(
          //                   'Scan your MEN QR code to add the diploma to your wallet',
          //                   style: TextStyle(
          //                       fontSize: 18, fontWeight: FontWeight.normal)),
          //             ),
          //           ),
          //         ),
          //       if (!state.bacDiploma.isEmpty())
          //         Card(
          //           child: Padding(
          //             padding: const EdgeInsets.all(16),
          //             child: Column(
          //               crossAxisAlignment: CrossAxisAlignment.start,
          //               children: <Widget>[
          //                 ListTile(
          //                   leading:
          //                       const Icon(Icons.vpn_key, color: Colors.blue),
          //                   title: Text('Serial: ${state.bacDiploma.serial}',
          //                       style: const TextStyle(
          //                           fontSize: 18, fontWeight: FontWeight.bold)),
          //                 ),
          //                 const SizedBox(height: 10),
          //                 ListTile(
          //                   leading:
          //                       const Icon(Icons.person, color: Colors.green),
          //                   title: Text('Name: ${state.bacDiploma.name}',
          //                       style: const TextStyle(
          //                           fontSize: 18,
          //                           fontWeight: FontWeight.normal)),
          //                 ),
          //                 const SizedBox(height: 10),
          //                 ListTile(
          //                   leading:
          //                       const Icon(Icons.business, color: Colors.red),
          //                   title: Text('CIF: ${state.bacDiploma.cif}',
          //                       style: const TextStyle(
          //                           fontSize: 18,
          //                           fontWeight: FontWeight.normal)),
          //                 ),
          //                 const SizedBox(height: 10),
          //                 ListTile(
          //                   leading: const Icon(Icons.credit_card,
          //                       color: Colors.purple),
          //                   title: Text('CNP: ${state.bacDiploma.cnp}',
          //                       style: const TextStyle(
          //                           fontSize: 18,
          //                           fontWeight: FontWeight.normal)),
          //                 ),
          //                 const SizedBox(height: 10),
          //                 ListTile(
          //                   leading: const Icon(Icons.date_range,
          //                       color: Colors.orange),
          //                   title: Text('Session: ${state.bacDiploma.session}',
          //                       style: const TextStyle(
          //                           fontSize: 18,
          //                           fontWeight: FontWeight.normal)),
          //                 ),
          //                 const SizedBox(height: 10),
          //                 ListTile(
          //                   leading:
          //                       const Icon(Icons.grade, color: Colors.yellow),
          //                   title: Text('Grade: ${state.bacDiploma.grade}',
          //                       style: const TextStyle(
          //                           fontSize: 18,
          //                           fontWeight: FontWeight.normal)),
          //                 ),
          //                 if (state.bacDiploma.signature != 'not signed')
          //                   const SizedBox(height: 10),
          //                 ListTile(
          //                   leading: state.bacDiploma.signature == 'not signed'
          //                       ? const Icon(Icons.cancel, color: Colors.red)
          //                       : const Icon(Icons.check, color: Colors.green),
          //                   title: Text(
          //                       'Signature: ${state.bacDiploma.signature}',
          //                       style: const TextStyle(
          //                           fontSize: 18,
          //                           fontWeight: FontWeight.normal)),
          //                 ),
          //                 if (state.bacDiploma.signature == 'not signed')
          //                   ElevatedButton(
          //                     onPressed: () {
          //                       // context.read<AppDataBloc>().signBacDiploma();
          //                     },
          //                     child: const Text('Sign diploma'),
          //                   ),
          //               ],
          //             ),
          //           ),
          //         ),
          //       if (!state.appliedToFaculty && !state.bacDiploma.isEmpty())
          //         const Card(
          //           child: Padding(
          //             padding: EdgeInsets.all(16),
          //             child: ListTile(
          //               leading:
          //                   Icon(Icons.tips_and_updates, color: Colors.amber),
          //               title: Text(
          //                   'Scan the faculty QR code to apply for the admission process',
          //                   style: TextStyle(
          //                       fontSize: 18, fontWeight: FontWeight.normal)),
          //             ),
          //           ),
          //         ),
          //       if (state.appliedToFaculty)
          //         const Card(
          //           child: Padding(
          //             padding: EdgeInsets.all(16),
          //             child: ListTile(
          //               leading: Icon(Icons.handshake, color: Colors.green),
          //               title: Text(
          //                   'You have applied to the Faculty of Engineering in Foreign Languages',
          //                   style: TextStyle(
          //                       fontSize: 18, fontWeight: FontWeight.normal)),
          //             ),
          //           ),
          //         ),
          //     ],
          //   ),
          // );
        },
      ),
    );
  }
}
