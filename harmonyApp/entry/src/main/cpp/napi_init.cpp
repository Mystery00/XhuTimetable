#include "libkn_api.h"
#include "napi/native_api.h"

static napi_value Undefined(napi_env env) {
    napi_value result = nullptr;
    napi_get_undefined(env, &result);
    return result;
}

static bool HasType(napi_env env, napi_value value, napi_valuetype expected) {
    napi_valuetype actual;
    return napi_typeof(env, value, &actual) == napi_ok && actual == expected;
}

static napi_value InitializeCore(napi_env env, napi_callback_info info) {
    size_t count = 4;
    napi_value arguments[4] = {};
    if (napi_get_cb_info(env, info, &count, arguments, nullptr, nullptr) != napi_ok || count != 4 ||
        !HasType(env, arguments[0], napi_string) || !HasType(env, arguments[1], napi_string) ||
        !HasType(env, arguments[2], napi_string) || !HasType(env, arguments[3], napi_object)) {
        napi_throw_type_error(env, nullptr, "Application directories, information and services are required");
        return nullptr;
    }
    napi_value nullValue;
    bool servicesNull = false;
    napi_get_null(env, &nullValue);
    napi_strict_equals(env, arguments[3], nullValue, &servicesNull);
    if (servicesNull) {
        napi_throw_type_error(env, nullptr, "Platform services are required");
        return nullptr;
    }
    HarmonyInitialize(env, arguments[0], arguments[1], arguments[2], arguments[3]);
    return Undefined(env);
}

static napi_value Dispatch(napi_env env, napi_callback_info info) {
    size_t count = 1;
    napi_value argument;
    if (napi_get_cb_info(env, info, &count, &argument, nullptr, nullptr) != napi_ok || count != 1 ||
        !HasType(env, argument, napi_string)) {
        napi_throw_type_error(env, nullptr, "A serialized request is required");
        return nullptr;
    }
    HarmonyDispatch(env, argument);
    return Undefined(env);
}

static napi_value DisposeCore(napi_env env, napi_callback_info info) {
    HarmonyDispose();
    return Undefined(env);
}

static napi_value SetNetworkState(napi_env env, napi_callback_info info) {
    size_t count = 1;
    napi_value argument;
    bool connected;
    if (napi_get_cb_info(env, info, &count, &argument, nullptr, nullptr) != napi_ok || count != 1 ||
        napi_get_value_bool(env, argument, &connected) != napi_ok) {
        napi_throw_type_error(env, nullptr, "Network state must be a boolean");
        return nullptr;
    }
    UpdateNetworkState(connected);
    return Undefined(env);
}

static napi_value Init(napi_env env, napi_value exports) {
    napi_property_descriptor properties[] = {
        {"initializeCore", nullptr, InitializeCore, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"dispatch", nullptr, Dispatch, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"disposeCore", nullptr, DisposeCore, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"updateNetworkState", nullptr, SetNetworkState, nullptr, nullptr, nullptr, napi_default, nullptr},
    };
    napi_define_properties(env, exports, sizeof(properties) / sizeof(properties[0]), properties);
    return exports;
}

static napi_module entryModule = {
    .nm_version = 1,
    .nm_flags = 0,
    .nm_filename = nullptr,
    .nm_register_func = Init,
    .nm_modname = "entry",
    .nm_priv = nullptr,
    .reserved = {0},
};

extern "C" __attribute__((constructor)) void RegisterEntryModule() {
    napi_module_register(&entryModule);
}
