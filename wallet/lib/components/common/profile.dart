import 'package:bac_web3/components/auth/auth_service.dart';
import 'package:flutter/material.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  void logout() async {
    await AuthService.instance.logout();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
      ),
      body: Center(
        child: CustomScrollView(
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
                        onTap: logout,
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
                                colors: [Colors.blue, Colors.purple],
                              ),
                              borderRadius: BorderRadius.all(Radius.circular(12.0)),
                            ),
                            child: const Center(
                              child: Icon(
                                Icons.logout,
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
                childCount: 1,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
