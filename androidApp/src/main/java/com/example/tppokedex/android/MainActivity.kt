package com.example.tppokedex.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tppokedex.DatabaseDriverFactory
import com.example.tppokedex.android.core.PokedexAdapter
import com.example.tppokedex.data.model.Pokedex
import com.example.tppokedex.android.databinding.ActivityMainBinding
import com.example.tppokedex.android.presentation.PokedexScreenState
import com.example.tppokedex.android.presentation.PokedexViewModel
import com.example.tppokedex.android.presentation.PokedexViewModelFactory
import com.example.tppokedex.data.model.PokedexResults
import com.example.tppokedex.repositoryDB.PokedexDBRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Declaración de variables para el adaptador, el ViewModel y el enlace de la actividad
    private lateinit var pokedexAdapter: PokedexAdapter
    private lateinit var viewModel: PokedexViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla el layout de la actividad principal y establece el contenido de la vista
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura el RecyclerView
        setupRecyclerView()

        // Inicializa el ViewModel usando un ViewModelProvider y un Factory
        viewModel = ViewModelProvider(this, PokedexViewModelFactory())[PokedexViewModel::class.java]

        // Lanza una coroutine para observar el estado de la pantalla del ViewModel
        lifecycleScope.launch {
            // Repite la tarea mientras el ciclo de vida esté en el estado CREATED
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // Recoge el estado de la pantalla del ViewModel
                viewModel.screenState.collect {
                    when (it) {
                        // Muestra la barra de progreso cuando el estado es Loading
                        PokedexScreenState.Loading -> showLoading()
                        // Maneja el error cuando el estado es Error
                        PokedexScreenState.Error -> handlerError()
                        // Muestra el Pokedex cuando el estado es ShowPokedex
                        is PokedexScreenState.ShowPokedex -> showPokedex(it.pokedex)
                    }
                }
            }
        }
    }

    // Configura el RecyclerView con un GridLayoutManager y un adaptador
    private fun setupRecyclerView() {
        pokedexAdapter = PokedexAdapter()

        // Crea un GridLayoutManager con 3 columnas
        val gridLayoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        with(binding.rvPokedex) {
            this.layoutManager = gridLayoutManager
            this.setHasFixedSize(true)
            this.adapter = pokedexAdapter
        }
    }

    // Muestra el Pokedex en el RecyclerView y guarda los datos en la base de datos
    private fun showPokedex(pokedex: Pokedex) {
        // Oculta la barra de progreso
        binding.pokedexProgressBar.visibility = View.GONE
        // Actualiza el adaptador con los resultados del Pokedex
        pokedexAdapter.updatePokedex(pokedex.results)

        // Crea una instancia del repositorio de la base de datos
        val repositoryPokedexBD = PokedexDBRepository(DatabaseDriverFactory(this))

        // Itera a través de los resultados de la API y realiza la inserción en la base de datos
        for (result in pokedex.results) {
            // Inserta el nombre y la URL en la tabla "Pokemons"
            repositoryPokedexBD.insertPokemon(result.name, result.url)
        }
    }

    // Maneja los errores al obtener los datos del Pokedex
    private fun handlerError() {
        // Crea una instancia del repositorio de la base de datos
        val repositoryPokedexBD  = PokedexDBRepository(databaseDriverFactory = DatabaseDriverFactory(this))
        // Obtiene todos los Pokémon de la base de datos
        val pokemon = repositoryPokedexBD.getAllPokemon()

        // Si no hay datos en la base de datos
        if (pokemon.isEmpty()){
            // Oculta el RecyclerView y la barra de progreso, muestra el layout de error
            binding.rvPokedex.visibility = View.GONE
            binding.pokedexProgressBar.visibility = View.GONE
            binding.errorLayout.visibility = View.VISIBLE
            Toast.makeText(this, "No hay nada para mostrar", Toast.LENGTH_LONG).show()
            // Configura el botón de reintentar para recargar la actividad
            binding.buttonReintentar.setOnClickListener {
                startActivity(Intent(this,MainActivity::class.java))
            }

        } else {
            // Oculta la barra de progreso y actualiza el adaptador con los datos de la base de datos
            binding.pokedexProgressBar.visibility = View.GONE
            pokedexAdapter.updatePokedex(pokemon)
            Toast.makeText(this, "No hay conexion a Internet", Toast.LENGTH_LONG).show()
        }
    }

    // Muestra la barra de progreso mientras se cargan los datos
    private fun showLoading() {
        binding.pokedexProgressBar.visibility = View.VISIBLE
    }
}
