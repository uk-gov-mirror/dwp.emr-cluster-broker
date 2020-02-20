locals {
  emrfs_em = {
    EncryptionConfiguration = {
      // TODO generate certs?
      EnableInTransitEncryption = false

      EnableAtRestEncryption = true
      AtRestEncryptionConfiguration = {
        S3EncryptionConfiguration = {
          EncryptionMode             = "CSE-Custom"
          S3Object                   = "${var.emp_provider}"
          EncryptionKeyProviderClass = "uk.gov.dwp.dataworks.dks.encryptionmaterialsprovider.DKSEncryptionMaterialsProvider"
        }
        LocalDiskEncryptionConfiguration = {
          EncryptionKeyProviderType = "AwsKms"
          AwsKmsKey                 = aws_kms_key.emr_sec_config.arn
          EnableEbsEncryption       = true
        }
      }
    }
  }
}

resource "aws_emr_security_configuration" "emr_sec_config" {
  name = var.name

  configuration = jsonencode(local.emrfs_em)
}
