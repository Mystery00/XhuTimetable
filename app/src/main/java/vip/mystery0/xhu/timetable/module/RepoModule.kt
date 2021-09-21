package vip.mystery0.xhu.timetable.module

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.repository.local.StartLocalRepo
import vip.mystery0.xhu.timetable.repository.remote.StartRemoteRepo

val repoModule = module {
    injectRepo(StartLocalRepo(), StartRemoteRepo())
}

const val SCOPE_LOCAL = "_Local"
const val SCOPE_REMOTE = "_Remote"

private inline fun <reified I, reified L : I, reified R : I> Module.injectRepo(
    local: L,
    remote: R
) {
    single<I>(named("$SCOPE_LOCAL${I::class.simpleName}")) { local }
    single<I>(named("$SCOPE_REMOTE${I::class.simpleName}")) { remote }
}

inline fun <reified INTER : Any> KoinComponent.repo(): Lazy<INTER> =
    if (isOnline()) {
        inject(named("$SCOPE_REMOTE${INTER::class.simpleName}"))
    } else {
        inject(named("$SCOPE_LOCAL${INTER::class.simpleName}"))
    }

inline fun <reified INTER : Any> KoinComponent.localRepo(): Lazy<INTER> =
    inject(named("$SCOPE_LOCAL${INTER::class.simpleName}"))

inline fun <reified INTER : Any> KoinComponent.getRepo(): INTER =
    if (isOnline()) {
        get(named("$SCOPE_REMOTE${INTER::class.simpleName}"))
    } else {
        get(named("$SCOPE_LOCAL${INTER::class.simpleName}"))
    }