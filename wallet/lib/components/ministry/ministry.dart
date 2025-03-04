import 'dart:io';

import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:bac_web3/common/constants.dart';
import 'package:bac_web3/components/auth/auth_service.dart';
import 'package:bac_web3/components/common/profile.dart';
import 'package:bac_web3/components/diploma/model/uploaded_diploma.dart';
import 'package:bac_web3/components/student/present_diploma_qr.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:http/http.dart' as http;
import 'package:path/path.dart' as path;

class MinistryPage extends StatefulWidget {
  const MinistryPage({super.key});

  @override
  State<MinistryPage> createState() => _MinistryPageState();
}

class _MinistryPageState extends State<MinistryPage> {
  void openProfilePage() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const ProfilePage()),
    );
  }

  signDiploma(UploadedDiploma uploadedDiplomaList) {
    context.read<AppDataBloc>().signBacDiploma(uploadedDiplomaList);
  }

  void presentDiploma(UploadedDiploma uploadedDiploma) {
    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => PresentDiplomaPage(diploma: uploadedDiploma)),
    );
  }

  void uploadDiploma() async {
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
          // show a snackbar
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Diploma a fost încărcată cu succes!'),
              duration: Duration(seconds: 3),
            ),
          );
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
                        onTap: uploadDiploma,
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
                                Icons.upload_file,
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
                  'Diplome deja încărcate',
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
                            state.uploadedDiplomaList[index].canBeSigned()
                                ? signDiploma(state.uploadedDiplomaList[index])
                                : presentDiploma(
                                    state.uploadedDiplomaList[index]);
                          },
                          child: Card(
                            child: Container(
                              decoration: BoxDecoration(
                                gradient: LinearGradient(
                                  begin: Alignment.topLeft,
                                  end: Alignment.bottomRight,
                                  colors: state.uploadedDiplomaList[index]
                                          .canBeSigned()
                                      ? [Colors.green, Colors.lightGreen]
                                      : [Colors.deepOrange, Colors.deepPurple],
                                ),
                                borderRadius: const BorderRadius.all(
                                    Radius.circular(12.0)),
                              ),
                              child: Center(
                                child: Padding(
                                  padding: const EdgeInsets.all(8.0),
                                  child: Icon(
                                    state.uploadedDiplomaList[index]
                                            .canBeSigned()
                                        ? Icons.draw_sharp
                                        : Icons.present_to_all,
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
