import 'package:json_annotation/json_annotation.dart';

part 'uploaded_diploma.g.dart';

@JsonSerializable()
class UploadedDiploma {
  final int id;
  final String description;
  @JsonKey(name: 'file_url')
  final String fileUrl;
  @JsonKey(defaultValue: '')
  final String signature;
  @JsonKey(name: 'created_at')
  final String createdAt;

  UploadedDiploma({
    required this.id,
    required this.description,
    required this.fileUrl,
    this.signature = '',
    required this.createdAt,
  });

  factory UploadedDiploma.fromJson(Map<String, dynamic> json) =>
      _$UploadedDiplomaFromJson(json);

  Map<String, dynamic> toJson() => _$UploadedDiplomaToJson(this);

  bool canBeSigned() {
    return signature.isEmpty;
  }
}
