import 'package:bac_web3/common/app_data_bloc.dart';
import 'package:bac_web3/common/constants.dart';
import 'package:bac_web3/components/auth/auth_service.dart';
import 'package:bac_web3/custom_theme.dart';
import 'package:bac_web3/router.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:logging/logging.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:uuid/uuid.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  var uuid = const Uuid();
  String traceId = uuid.v1();
  Logger.root.level = LOGGING_LEVEL;
  Logger.root.onRecord.listen((record) {
    if (kDebugMode) {
      print(
          '${record.level.name}: ${record.time}: [$traceId] ${record.message}');
    }
  });

  await AuthService.instance.init();

  runApp(
    MultiBlocProvider(
      providers: [
        BlocProvider<AppDataBloc>(
          create: (BuildContext localContext) {
            return AppDataBloc();
          },
        ),
      ],
      child: const Bac3(),
    ),
  );
}

class Bac3 extends StatefulWidget {
  const Bac3({super.key});

  @override
  State<Bac3> createState() => _Bac3State();
}

class _Bac3State extends State<Bac3> {
  @override
  void initState() {
    super.initState();
    _requestCameraPermission();
  }

  Future<void> _requestCameraPermission() async {
    var status = await Permission.camera.status;
    if (!status.isGranted) {
      status = await Permission.camera.request();
      if (!status.isGranted) {
        // TODO SHOW BIG ERROR
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      routerConfig: router,
      debugShowCheckedModeBanner: false,
      title: 'BAC3.0 Wallet',
      theme: CustomTheme.lightTheme,
    );
  }
}
