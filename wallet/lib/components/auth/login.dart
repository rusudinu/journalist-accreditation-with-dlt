import 'package:bac_web3/common/components/login_button.dart';
import 'package:bac_web3/components/auth/auth_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:flutter_svg/flutter_svg.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
  }

  void authenticate() async {
    setState(() {
      _isLoading = true;
    });

    final result = await AuthService.instance.login();

    if (result != 'SUCCESS') {
      final snackBar = SnackBar(
        content: Text(result),
      );

      //ScaffoldMessenger.of(context).showSnackBar(snackBar);
    }

    setState(() {
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBodyBehindAppBar: true,
      body: Container(
        decoration: const BoxDecoration(
          image: DecorationImage(
            image: AssetImage('assets/png/login_background.png'),
            fit: BoxFit.cover,
          ),
        ),
        child: Container(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [
                Colors.black.withOpacity(.4),
                Colors.black.withOpacity(.5),
              ],
              begin: Alignment.bottomCenter,
              end: Alignment.topCenter,
            ),
          ),
          child: SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 30),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    children: [
                      Padding(
                        padding: const EdgeInsets.only(bottom: 20),
                        child: SvgPicture.asset(
                          'assets/svg/wallet.svg',
                          height: 100,
                          width: 100,
                        ),
                      ),
                      Text(
                        'BAC3 Wallet',
                        style: Theme.of(context)
                            .textTheme
                            .headlineLarge
                            ?.copyWith(color: Colors.white),
                      ),
                    ],
                  ),
                  if (_isLoading)
                    const Align(
                      alignment: Alignment.center,
                      child: SpinKitSpinningLines(
                        color: Colors.white,
                        size: 50.0,
                      ),
                    ),
                  if (!_isLoading)
                    LoginButton(
                      text: 'AUTENTIFICARE',
                      iconObj: Icons.email,
                      onClick: authenticate,
                    ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
