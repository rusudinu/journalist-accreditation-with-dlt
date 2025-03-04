// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'uploaded_diploma.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UploadedDiploma _$UploadedDiplomaFromJson(Map<String, dynamic> json) =>
    UploadedDiploma(
      id: (json['id'] as num).toInt(),
      description: json['description'] as String,
      fileUrl: json['file_url'] as String,
      signature: json['signature'] as String? ?? '',
      createdAt: json['created_at'] as String,
    );

Map<String, dynamic> _$UploadedDiplomaToJson(UploadedDiploma instance) =>
    <String, dynamic>{
      'id': instance.id,
      'description': instance.description,
      'file_url': instance.fileUrl,
      'signature': instance.signature,
      'created_at': instance.createdAt,
    };
