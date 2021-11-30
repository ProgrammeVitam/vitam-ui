#!/bin/perl
################################################################################
# Description: This script allows you to easily rekey your vaults
#              and/or initialize your vaults with secured random passwords
################################################################################

use strict;
use warnings;
use Getopt::Long;

################################################################################
# Configuration

## Define authorized characters for your passwords
### Warning: Some passwords doesn't accept _ or other special characters (especially when used on url); that's why we avoid them.
my @chars = ("A".."Z", "a".."z", "0".."9");

## Define the default length of your passwords
my $default_length = 32;
my $min_length     = 12;

## List of managed vault files
my @vault_files = (
  'environments/group_vars/all/vault-keystores.yml',
  'environments/group_vars/all/vault-mongodb.yml',
  'environments/group_vars/all/vault-vitamui.yml',
  'environments/vitamui_extra_vault.yml'
);

my @pki_files = (
  'environments/certs/vault-ca.yml',
  'environments/certs/vault-certs.yml'
);

################################################################################

my $rekey  = undef;
my $init   = undef;
my $length = undef;
my $help   = undef;
GetOptions( "r|rekey:s"  => \$rekey,
            "i|init:s"   => \$init,
            "l|length:i" => \$length,
            "h|help"     => \$help );

if ( defined($help) ) {
  print "usage: $0 [-r|--rekey=YES] [-i|--init=YES] [-l|--length=32] [-h|--help]\n\n";
  print "Options:\n";
  print "  -r=YES, --rekey=YES  Encrypt your vaults with a new random password\n";
  print "                       It will create a new vault_pass.txt & vault_pki.pass and encrypt your vaults files with them.\n";
  print "  -i=YES, --init=YES   Initialize your vaults, from .example files, with random passwords\n";
  print "                       It will update all the tagged 'changeit' passwords with a new one.\n";
  print "  -l=32,  --length=32  Define the length of the passwords (if unset or under $min_length, default set to $default_length).\n";
  print "  -h, --help           Print the usage.\n";
  exit 0;
}

################################################################################
# Check the length
if ( defined($length) ) {

  if ( $length >= $min_length ) {

    $default_length = $length;

  } else {

    print "WARNING: Parameter --length=$length is lower than $min_length, for security reason we use the default value ($default_length).\n\n";

  }

}

################################################################################
# Manage vault passwords
if ( !defined($rekey) or $rekey eq '' ) {

  print "WARNING: It will create a new vault_pass.txt & vault_pki.pass and encrypt your vaults files with them.\n";
  print "Do you want to encrypt your vaults with a new password ? YES/[NO] : ";
  $rekey = <STDIN>;
  chomp $rekey;

}

if( "$rekey" eq 'YES' ) {

  rekey('vault_pass.txt', @vault_files);
  rekey('vault_pki.pass', @pki_files);

} else {

  print "Skipping rekey for vaults.\n";

}

print "\n";

################################################################################
# Initialize vaults with new random passwords from vault's example files.
if ( !defined($init) or $init eq '' ) {

  print "WARNING: It will erase your current vault files with values according to the .example files.\n";
  print "Do you want to initialize your vaults (from example files) with new random passwords ? YES/[NO] : ";
  $init = <STDIN>;
  chomp $init;

}

if ( "$init" eq 'YES' ) {

  # my $consul_encrypt=`./pki/scripts/generate_consul_key.sh`;
  # chomp $consul_encrypt;

  foreach my $file ( @vault_files ) {

    if ( -f "${file}.example" ){
      print "=> Initialize passwords from $file.example for $file\n";

      open my $in,  '<', "$file.example" or die $!;
      open my $out, '>', "$file";

      while ( <$in> ) {

        my $pass = random_pass();

        # s/(^consul_encrypt:\s*).+$/$1$consul_encrypt/;
        s/(\s*\w+: )(change_it|changeit)\w+/$1$pass/;
        print $out $_;

      }

      close $out;

      # Encrypt with vault_pass.txt
      print "$file - ";
      system("ansible-vault encrypt --vault-password-file vault_pass.txt $file");

    } else {

      print "Skipping password initialization: Could not find ${file}.example file."

    }

  }

} else {

  print "Skipping passwords initialization for vaults.\n";

}

################################################################################
# Function to rekey a list of vaults
# Parameters:
#   1. vault_pass_file: The original vault_pass file used to encrypt the vaults.
#   2. files: The list of vaults to be reencrypted.
sub rekey {

  my($vault_pass_file, @files) = @_;

  if ( -f $vault_pass_file ) {

    rename $vault_pass_file, "${vault_pass_file}-ori";

  } else {

    print "\nWARNING: Could not find file ${vault_pass_file}.\n";
    print "Please enter the password for ${vault_pass_file}: \n";
    system("stty -echo");
    my $password = <STDIN>;
    chomp $password;
    system("stty echo");

    # If we have a previous password, we store it in an -ori file
    if ( $password ne "" ) {

      open my $ori, '>', "${vault_pass_file}-ori";
      print $ori $password;
      close $ori;
      print "\n=> Stored original password under ${vault_pass_file}-ori\n";

    } else {

      print "Empty ${vault_pass_file}\n";

    }
  }

  print "=> Create new password for ${vault_pass_file}\n";
  open my $vault_pass, '>', ${vault_pass_file};
  print $vault_pass random_pass();
  close $vault_pass;

  foreach my $file ( @files ) {

    if ( -f $file ){

      print "$file - ";
      # If we have an original vault file
      if ( -f "${vault_pass_file}-ori" ) {

        system("ansible-vault rekey --new-vault-password-file ${vault_pass_file} --vault-password-file ${vault_pass_file}-ori $file");

      } else {

        system("ansible-vault encrypt --vault-password-file ${vault_pass_file} $file");

      }

    } else {

      print "WARNING: Could not find file ${file}, skipping rekey...\n";

    }

  }

}

################################################################################
# Function to generate a random password
sub random_pass {

  my $pass;
  $pass .= $chars[rand @chars] for 1..$default_length;

  return $pass;

}
