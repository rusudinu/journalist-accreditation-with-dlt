import 'package:bac_web3/components/verifier/scan_vp.dart';
import 'package:flutter/material.dart';

enum AddDiplomaMethod { qrScanner, addManually, notSelected }

class AddDiplomaToWallet extends StatefulWidget {
  const AddDiplomaToWallet({super.key});

  @override
  State<AddDiplomaToWallet> createState() => _AddDiplomaToWalletState();
}

class _AddDiplomaToWalletState extends State<AddDiplomaToWallet> {
  AddDiplomaMethod _selectedMethod = AddDiplomaMethod.notSelected;
  TextEditingController _textController = TextEditingController();

  void qrScanner() {
    setState(() {
      _selectedMethod = AddDiplomaMethod.qrScanner;
    });
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const ScanVP()),
    );
  }

  void addDiplomaManually() {
    if (_selectedMethod == AddDiplomaMethod.addManually) {
      // TODO later actually add diploma
      // show a success message
      _textController.clear();
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Document added successfully'),
        backgroundColor: Colors.green,
      ));
      setState(() {
        _selectedMethod = AddDiplomaMethod.notSelected;
      });
      return;
    }
    setState(() {
      _selectedMethod = AddDiplomaMethod.addManually;
    });
  }

  // close and return to the
  // page with the two options
  void closeAddMethod() {
    setState(() {
      _selectedMethod = AddDiplomaMethod.notSelected;
    });
  }

  // close the page
  void close() {
    Navigator.pop(context);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Add Document'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              _selectedMethod == AddDiplomaMethod.addManually
                  ? TextField(
                      decoration: const InputDecoration(
                        hintText: 'Introdu codul diplomei',
                      ),
                      controller: _textController,
                      keyboardType: TextInputType.multiline,
                      maxLines: null,
                    )
                  : GestureDetector(
                      onTap: qrScanner,
                      child: Card(
                        child: Container(
                          decoration: const BoxDecoration(
                            gradient: LinearGradient(
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight,
                              colors: [Colors.green, Colors.lightGreen],
                            ),
                            borderRadius: BorderRadius.all(Radius.circular(12.0)),
                          ),
                          child: const Center(
                            child: Padding(
                              padding: EdgeInsets.all(8.0),
                              child: Column(
                                children: [
                                  Icon(
                                    Icons.qr_code_scanner_sharp,
                                    color: Colors.white,
                                    size: 40,
                                  ),
                                  Text(
                                    'Scan the credential QR code',
                                    style: TextStyle(color: Colors.white),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
              GestureDetector(
                onTap: addDiplomaManually,
                child: Card(
                  child: Container(
                    decoration: const BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                        colors: [Colors.green, Colors.lightGreen],
                      ),
                      borderRadius: BorderRadius.all(Radius.circular(12.0)),
                    ),
                    child: Center(
                      child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Column(
                          children: [
                            _selectedMethod == AddDiplomaMethod.addManually
                                ? const SizedBox()
                                : const Icon(
                                    Icons.text_fields,
                                    color: Colors.white,
                                    size: 40,
                                  ),
                            Text(
                              _selectedMethod == AddDiplomaMethod.addManually ? 'Add credential' : 'Add credential manually',
                              style: const TextStyle(color: Colors.white),
                            ),
                          ],
                        ),
                      ),
                    ),
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
