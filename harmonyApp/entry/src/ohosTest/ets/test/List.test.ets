import loginEncryptionTest from './LoginEncryption.test';
import ohosServicesTest from './OhosServices.test';
import nativeUiTest from './NativeUi.test';

export default function testsuite(): void {
  loginEncryptionTest();
  ohosServicesTest();
  nativeUiTest();
}
