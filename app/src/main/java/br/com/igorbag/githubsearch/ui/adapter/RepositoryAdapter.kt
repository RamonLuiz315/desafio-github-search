package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository


class RepositoryAdapter(
    private var repositories: List<Repository>,
    private val onItemClick: (String) -> Unit,
    private val onShareClick: (String) -> Unit
) : RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>() {

    // ViewHolder que o Adapter usa
    inner class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txtName: TextView = itemView.findViewById(R.id.txtRepoName)
        private val btnShare: ImageButton = itemView.findViewById(R.id.btnShareRepo)

        fun bind(repository: Repository) {
            txtName.text = repository.name

            // clique no item abre o navegador
            itemView.setOnClickListener {
                onItemClick(repository.htmlUrl)
            }

            // clique no botão share compartilha o link
            btnShare.setOnClickListener {
                onShareClick(repository.htmlUrl)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repository, parent, false)
        return RepositoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        holder.bind(repositories[position])
    }

    override fun getItemCount(): Int = repositories.size

    fun submitList(newList: List<Repository>) {
        repositories = newList
        notifyDataSetChanged()
    }
}
