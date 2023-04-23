package vip.mystery0.xhu.timetable.module

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.repository.local.CourseLocalRepo
import vip.mystery0.xhu.timetable.repository.local.NoticeLocalRepo
import vip.mystery0.xhu.timetable.repository.network.CourseNetworkRepo
import vip.mystery0.xhu.timetable.repository.network.NoticeNetworkRepo
import vip.mystery0.xhu.timetable.repository.remote.CourseRemoteRepo
import vip.mystery0.xhu.timetable.repository.remote.NoticeRemoteRepo

val repoModule = module {
    injectRepo(CourseLocalRepo(), CourseRemoteRepo(), CourseNetworkRepo)
    injectRepo(NoticeLocalRepo(), NoticeRemoteRepo(), NoticeNetworkRepo)
}

const val SCOPE_LOCAL = "_Local"
const val SCOPE_REMOTE = "_Remote"
const val SCOPE_NETWORK = "_Network"

inline fun <reified R : Repo> _localName(): StringQualifier =
    named("$SCOPE_LOCAL${R::class.simpleName}")

inline fun <reified R : Repo> _remoteName(): StringQualifier =
    named("$SCOPE_REMOTE${R::class.simpleName}")

inline fun <reified R : Repo> _networkName(): StringQualifier =
    named("$SCOPE_NETWORK${R::class.simpleName}")

private inline fun <reified I : Repo, reified L : I, reified R : I> Module.injectRepo(
    local: L,
    remote: R,
    network: I? = null
) {
    single<I>(_localName<I>()) { local }
    single<I>(_remoteName<I>()) { remote }
    if (network != null) {
        single(_networkName<I>()) { network }
    }
}

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

inline fun <reified INTER : Repo> getLocalRepo(): INTER =
    KoinJavaComponent.get(INTER::class.java, _localName<INTER>())

inline fun <reified INTER : Repo> getRemoteRepo(): INTER =
    KoinJavaComponent.get(INTER::class.java, _remoteName<INTER>())

interface Repo : KoinComponent

interface NetworkRepo<R : Repo> : KoinComponent {
    val local: R
    val remote: R

    fun dispatch(): R = if (isOnline()) remote else local
}