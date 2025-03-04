class Diploma {
  final String serial;
  final String name;
  final String cif;
  final String cnp;
  final String session;
  final String grade;
  final String signature;

  Diploma({
    required this.serial,
    required this.name,
    required this.cif,
    required this.cnp,
    required this.session,
    required this.grade,
    this.signature = 'not signed',
  });

  factory Diploma.fromJson(Map<String, dynamic> json) {
    return Diploma(
      serial: json['serial'],
      name: json['name'],
      cif: json['cif'],
      cnp: json['cnp'],
      session: json['session'],
      grade: json['grade'],
      signature: json['signature'] ?? 'not signed',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'serial': serial,
      'name': name,
      'cif': cif,
      'cnp': cnp,
      'session': session,
      'grade': grade,
      'signature': signature,
    };
  }

  factory Diploma.empty() {
    return Diploma(
      serial: '',
      name: '',
      cif: '',
      cnp: '',
      session: '',
      grade: '',
      signature: '',
    );
  }

  bool isEmpty() {
    return serial.isEmpty &&
        name.isEmpty &&
        cif.isEmpty &&
        cnp.isEmpty &&
        session.isEmpty &&
        grade.isEmpty;
  }
}
