import loginEncryptionTest from './LoginEncryption.test';
import ohosServicesTest from './OhosServices.test';

export default function testsuite(): void {
  loginEncryptionTest();
  ohosServicesTest();
}
