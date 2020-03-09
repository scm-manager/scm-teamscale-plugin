package com.cloudogu.scm.teamscale.config;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XmlEncryptionAdapter extends XmlAdapter<String, String> {

  @Override
  public String marshal(String v) {
    if (!EncryptionUtil.isEncrypted(v)) {
      v = EncryptionUtil.encrypt(v);
    }

    return v;
  }

  @Override
  public String unmarshal(String v){
    if (EncryptionUtil.isEncrypted(v)) {
      v = EncryptionUtil.decrypt(v);
    }

    return v;
  }
}

