package vip.mystery0.xhu.timetable.module

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import vip.mystery0.xhu.timetable.isOnline

const val SCOPE_LOCAL = "_Local"
const val SCOPE_REMOTE = "_Remote"
const val SCOPE_NETWORK = "_Network"

inline fun <reified R : Repo> _localName(): StringQualifier =
    named("$SCOPE_LOCAL${R::class.simpleName}")

inline fun <reified R : Repo> _remoteName(): StringQualifier =
    named("$SCOPE_REMOTE${R::class.simpleName}")

inline fun <reified R : Repo> _networkName(): StringQualifier =
    named("$SCOPE_NETWORK${R::class.simpleName}")

inline fun <reified INTER : Repo> KoinComponent.localRepo(): Lazy<INTER> =
    inject(_localName<INTER>())

inline fun <reified INTER : Repo> KoinComponent.remoteRepo(): Lazy<INTER> =
    inject(_remoteName<INTER>())

inline fun <reified INTER : Repo> KoinComponent.getRepo(): INTER {
    val network = getKoin().getOrNull<INTER>(_networkName<INTER>())
    if (network != null) {
        return network
    }
    return if (isOnline()) {
        get(_remoteName<INTER>())
    } else {
        get(_localName<INTER>())
    }
}

interface Repo : KoinComponent