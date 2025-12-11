package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityAppBinding
import androidx.navigation.findNavController
import ru.netology.nmedia.fragments.FeedFragment.Companion.textArgs
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory


class AppActivity : AppCompatActivity() {
    private val dependencyContainer = DependencyContainer.getInstance()

    private val viewModel by viewModels<AuthViewModel>(
        factoryProducer = { ViewModelFactory(dependencyContainer.repository, dependencyContainer.appAuth, dependencyContainer.apiService) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)




        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestNotificationsPermission()

        // === ИСПРАВЛЕНИЕ №1: Наблюдаем за LiveData, а не за статическим геттером ===
        // При изменении данных в AppAuth, этот обсервер срабатывает и вызывает invalidateOptionsMenu()
        viewModel.data.observe(this) { token ->
            Log.d("AppActivityLifecycle", "Токен изменился. Вызываем invalidateOptionsMenu().")
            invalidateOptionsMenu() // <-- Это вызывает повторный вызов onCreateMenu
        }
        // ========================================================================

        // Подписываемся на событие навигации из AuthViewModel
        viewModel.navigateToSignInEvent.observe(this) {
            // Выполняем переход на фрагмент аутентификации
            findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)
        }


        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.menu_main, menu)


                  menu.let {
                      val isAuthorized = viewModel.data.value?.id != null
                      Log.d("MyTag1111", "viewModel.isAutorized: $isAuthorized")
                        it.setGroupVisible(R.id.unauthorized, !isAuthorized)
                        it.setGroupVisible(R.id.authorized, isAuthorized)
                    }

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.signin -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)
//                            AppAuth.getInstance().setAuth(5, "x-token")
                            false
                        }

                        R.id.signup -> {
                            // TODO: just hardcode it, implementation must be in homework
                            dependencyContainer.appAuth.setAuth(5, "x-token")
                            true
                        }

                        R.id.logout -> {
//                            Log.d("MyTag_Logout", "Кнопка 'Выход' (R.id.logout) была нажата.")
////                            AppAuth.getInstance().removeAuth()
//                            this@AppActivity.viewModel.logout()
//                            Log.d("MyTag_AppActivity", "Обработка выхода по умолчанию в Activity.")
//                            false
                            val currentDestination = findNavController(R.id.nav_host_fragment).currentDestination
                            if (currentDestination?.id != R.id.newPostFragment) {
                                Log.d("MyTag_Logout", "Кнопка 'Выход' (R.id.logout) была нажата.")
                                viewModel.logout()
                                Log.d("MyTag_AppActivity", "Обработка выхода по умолчанию в Activity.")
                                true
                            } else {
                                false // Позволяем фрагменту обработать logout
                            }
                        }

                        else -> false
                    }
            }


        )





        intent?.let {
            if (it.action != Intent.ACTION_SEND) return@let

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                Snackbar.make(
                    binding.root,
                    R.string.error_empty_content,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        finish()
                    }.show()

                return@let
            }
            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    textArgs = text
                }
            )

        }

        checkGoogleApiAvailability()

    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(
                this@AppActivity,
                "___R.string.google_play_unavailable___",
                Toast.LENGTH_LONG
            ).show()
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            println(it)
        }
    }

}