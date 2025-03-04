import 'dart:convert';

import 'package:bac_web3/components/diploma/model/uploaded_diploma.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:qr_flutter/qr_flutter.dart';

class PresentDiplomaPage extends StatelessWidget {
  final UploadedDiploma diploma;

  const PresentDiplomaPage({super.key, required this.diploma});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Diploma'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Text(diploma.createdAt),
              const SizedBox(height: 10),
              QrImageView(
                data: jsonEncode(diploma.toJson()),
                size: MediaQuery.of(context).size.width * 0.8,
              ),
              IconButton(
                icon: const Icon(Icons.copy),
                onPressed: () {
                  Clipboard.setData(
                      ClipboardData(text: jsonEncode(diploma.toJson())));
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
