import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:bac_web3/components/student/student.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AppDataBloc, AppDataState>(
      builder: (context, state) {
        return Scaffold(
          appBar: AppBar(
            title: const Text('Digital Wallet'),
          ),
          body: const Center(
            child: StudentPage(),
          ),
        );
      },
    );
  }
}
