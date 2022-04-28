use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_org_electronvolts_evlib_jni_Native_getStr(
    env: JNIEnv,
    _: JClass,
    input: JString,
) -> jstring {
    let input: String = env.get_string(input).unwrap().into();
    let output = env.new_string(format!("Hello, {input}!")).unwrap();
    output.into_inner()
}