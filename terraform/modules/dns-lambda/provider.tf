provider "aws" {
  region  = var.region
  version = "~> 2.46.0"

  assume_role {
    role_arn = var.role_arn
  }
}
