import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:bac_web3/components/common/profile.dart';
import 'package:bac_web3/components/diploma/model/uploaded_diploma.dart';
import 'package:bac_web3/components/student/add_diploma_to_wallet.dart';
import 'package:bac_web3/components/student/present_diploma_by_scanning.dart';
import 'package:bac_web3/components/student/present_diploma_qr.dart';
import 'package:bac_web3/components/verifier/scan_vp.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class StudentPage extends StatefulWidget {
  const StudentPage({super.key});

  @override
  State<StudentPage> createState() => _StudentPageState();
}

class _StudentPageState extends State<StudentPage> {
  void openProfilePage() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const ProfilePage()),
    );
  }

  void addDiplomaToWallet() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const AddDiplomaToWallet()),
    );
  }

  void presentDiploma(UploadedDiploma uploadedDiploma) {
    // Navigator.push(
    //   context,
    //   MaterialPageRoute(
    //       builder: (context) => PresentDiplomaPage(diploma: uploadedDiploma)),
    // );
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const PresentDiplomaByScanning()),
    );
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AppDataBloc, AppDataState>(
      buildWhen: (previous, current) =>
          previous.uploadedDiplomaList != current.uploadedDiplomaList,
      builder: (context, state) {
        return CustomScrollView(
          slivers: [
            SliverGrid(
              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: MediaQuery.of(context).size.width > 600 ? 4 : 2,
                crossAxisSpacing: 4.0,
                mainAxisSpacing: 4.0,
              ),
              delegate: SliverChildBuilderDelegate(
                (BuildContext context, int index) {
                  switch (index) {
                    case 0:
                      return GestureDetector(
                        onTap: openProfilePage,
                        child: Card(
                          elevation: 6.0,
                          shadowColor: Colors.grey[60],
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12.0),
                          ),
                          child: Container(
                            decoration: const BoxDecoration(
                              gradient: LinearGradient(
                                begin: Alignment.topLeft,
                                end: Alignment.bottomRight,
                                colors: [Colors.amber, Colors.deepOrange],
                              ),
                              borderRadius:
                                  BorderRadius.all(Radius.circular(12.0)),
                            ),
                            child: const Center(
                              child: Icon(
                                Icons.person,
                                color: Colors.white,
                                size: 50,
                              ),
                            ),
                          ),
                        ),
                      );
                    case 1:
                      return GestureDetector(
                        onTap: addDiplomaToWallet,
                        child: Card(
                          elevation: 6.0,
                          shadowColor: Colors.grey[60],
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12.0),
                          ),
                          child: Container(
                            decoration: const BoxDecoration(
                              gradient: LinearGradient(
                                begin: Alignment.topLeft,
                                end: Alignment.bottomRight,
                                colors: [Colors.cyan, Colors.indigo],
                              ),
                              borderRadius:
                                  BorderRadius.all(Radius.circular(12.0)),
                            ),
                            child: const Center(
                              child: Icon(
                                Icons.document_scanner_rounded,
                                color: Colors.white,
                                size: 50,
                              ),
                            ),
                          ),
                        ),
                      );
                  }
                  return null;
                },
                childCount: 2,
              ),
            ),
            const SliverToBoxAdapter(
              child: Padding(
                padding: EdgeInsets.only(top: 16, left: 6),
                child: Text(
                  'Diplomele tale',
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
            SliverList.builder(
              itemBuilder: (context, index) {
                return Card(
                  child: Padding(
                    padding:
                        const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        ListTile(
                          leading: state.uploadedDiplomaList[index]
                                  .canBeSigned()
                              ? const Icon(Icons.cancel, color: Colors.red)
                              : const Icon(Icons.check, color: Colors.green),
                          title: Text(
                              state.uploadedDiplomaList[index].canBeSigned()
                                  ? 'Nu a fost inca semnata'
                                  : 'Semnătura: ${state.uploadedDiplomaList[index].signature}',
                              style: const TextStyle(
                                  fontSize: 18, fontWeight: FontWeight.normal)),
                        ),
                        GestureDetector(
                          onTap: () {
                            presentDiploma(state.uploadedDiplomaList[index]);
                          },
                          child: Card(
                            child: Container(
                              decoration: const BoxDecoration(
                                gradient: LinearGradient(
                                  begin: Alignment.topLeft,
                                  end: Alignment.bottomRight,
                                  colors: [
                                    Colors.deepOrange,
                                    Colors.deepPurple
                                  ],
                                ),
                                borderRadius:
                                    BorderRadius.all(Radius.circular(12.0)),
                              ),
                              child: const Center(
                                child: Padding(
                                  padding: EdgeInsets.all(8.0),
                                  child: Icon(
                                    Icons.present_to_all,
                                    color: Colors.white,
                                    size: 40,
                                  ),
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
              itemCount: state.uploadedDiplomaList.length,
            ),
          ],
        );
      },
    );
  }
}
