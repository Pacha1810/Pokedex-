package com.example.tppokedex.android.core

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tppokedex.android.R
import com.example.tppokedex.application.ImageBuilder
import com.example.tppokedex.application.StringFormatter
import com.example.tppokedex.android.databinding.ItemPokedexBinding
import com.example.tppokedex.data.model.PokedexResults
import com.squareup.picasso.Picasso

class PokedexAdapter : RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder>() {

    private val pokemonList = mutableListOf<PokedexResults>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokedexViewHolder {
        val pokedexBinding = ItemPokedexBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PokedexViewHolder(pokedexBinding)
    }

    override fun onBindViewHolder(holder: PokedexViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        holder.binding.titleTextView.text = StringFormatter.changeFirstLetterToUppercaseAndDeleteMiddleDash(pokemon.name)

        Picasso.get()
            .load(ImageBuilder.buildPokemonImageByUrl(pokemon.url))
            .placeholder(R.drawable.pokeball)
            .error(R.drawable.pokeball)
            .into(holder.binding.iconImageView)
    }

    override fun getItemCount(): Int {
        return pokemonList.size
    }

    fun updatePokedex(results: List<PokedexResults>) {
        pokemonList.clear()
        if(results != null) {
            pokemonList.addAll(results)
        }
        notifyDataSetChanged()
    }



    class PokedexViewHolder(val binding: ItemPokedexBinding) :
        RecyclerView.ViewHolder(binding.root)
}