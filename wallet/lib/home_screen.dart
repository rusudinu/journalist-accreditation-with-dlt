import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:bac_web3/components/auth/auth_service.dart';
import 'package:bac_web3/components/ministry/ministry.dart';
import 'package:bac_web3/components/student/student.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  // int _selectedIndex = 1;
  // men should be able to upload diploma and allow the
  // kids to scan it such that
  // it is then stored in their wallets

  // kids should be able to scan men diplomas
  // and present them to upb to verify
  bool isMinistry =
      AuthService.instance.logininfo.userRole == 'ministry@ministry.com';

  @override
  void initState() {
    super.initState();
    AuthService.instance.logininfo.addListener(() {
      setState(() {
        isMinistry =
            AuthService.instance.logininfo.userRole == 'ministry@ministry.com';
      });
    });
  }

  //
  // final List<Widget> _widgetOptions = <Widget>[
  //   const DevPage(),
  //   const HomePage(),
  //   const ScanVP(),
  //   const ScanAdmission(),
  // ];
  //
  // void _onItemTapped(int index) {
  //   setState(() {
  //     _selectedIndex = index;
  //   });
  // }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AppDataBloc, AppDataState>(
      builder: (context, state) {
        return Scaffold(
          appBar: AppBar(
            title: Text('BAC3.0 Wallet - ${isMinistry ? 'MEN' : 'Elev'}'),
          ),
          body: Center(
            child: isMinistry ? const MinistryPage() : const StudentPage(),
          ),
        );
      },
    );
  }
}
