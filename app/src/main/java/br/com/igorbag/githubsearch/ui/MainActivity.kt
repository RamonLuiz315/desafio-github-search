package br.com.igorbag.githubsearch.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var edtUsername: EditText
    private lateinit var btnSaveAndOpen: Button
    private lateinit var btnResetUser: Button
    private lateinit var rvRepositories: RecyclerView   // <-- IMPORTANTE
    // Retrofit
    private lateinit var gitHubApi: GitHubService

    // Lista usada pelo Adapter
    private val repositories = mutableListOf<Repository>()
    private lateinit var adapter: RepositoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupView()
        setupListeners()
        setupRetrofit()
        showUserName()
    }

    // Recupera os IDs da tela e configura o RecyclerView + Adapter
    private fun setupView() {
        edtUsername    = findViewById(R.id.edtUsername)
        btnSaveAndOpen = findViewById(R.id.btnSaveAndOpen)
        btnResetUser   = findViewById(R.id.btnResetUser)
        rvRepositories = findViewById(R.id.rvRepositories)  // <-- usa o mesmo ID do XML

        adapter = RepositoryAdapter(
            repositories,
            { urlRepository -> openBrowser(urlRepository) },
            { urlRepository -> shareRepositoryLink(urlRepository) }
        )

        rvRepositories.layoutManager = LinearLayoutManager(this)
        rvRepositories.adapter = adapter
    }





    // Listeners dos botões
    private fun setupListeners() {
        btnSaveAndOpen.setOnClickListener {
            saveUserLocal()
            getAllReposByUserName()
        }

        btnResetUser.setOnClickListener {
            edtUsername.setText("")

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            repositories.clear()
            adapter.notifyDataSetChanged()
        }
    }

    // Salva usuário no SharedPreferences
    private fun saveUserLocal() {
        val user = edtUsername.text.toString()

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_NAME, user)
            .apply()
    }

    // Carrega usuário salvo e, se tiver, já busca os repositórios
    private fun showUserName() {
        val shared = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedUser = shared.getString(KEY_USER_NAME, "")

        if (!savedUser.isNullOrEmpty()) {
            edtUsername.setText(savedUser)
            getAllReposByUserName()
        }
    }

    // Configura Retrofit
    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")               // URL base da API do GitHub
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        gitHubApi = retrofit.create(GitHubService::class.java)
    }

    // Busca todos os repositórios do usuário informado
    private fun getAllReposByUserName() {
        val user = edtUsername.text.toString().trim()
        if (user.isEmpty()) return

        gitHubApi.getAllRepositoriesByUser(user)
            .enqueue(object : Callback<List<Repository>> {
                override fun onResponse(
                    call: Call<List<Repository>>,
                    response: Response<List<Repository>>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body().orEmpty()
                        updateRepositoryList(list)
                    }
                }

                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    // Aqui você pode exibir um Toast de erro se quiser
                    // Toast.makeText(this@MainActivity, "Erro ao buscar repositórios", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Atualiza lista usada pelo Adapter
    private fun updateRepositoryList(list: List<Repository>) {
        repositories.clear()
        repositories.addAll(list)
        adapter.notifyDataSetChanged()
    }

    // Compartilhar link do repositório
    private fun openBrowser(urlRepository: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlRepository))
        startActivity(intent)
    }

    private fun shareRepositoryLink(urlRepository: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }


    companion object {
        private const val PREFS_NAME = "github_prefs"
        private const val KEY_USER_NAME = "user_name"
    }
}
