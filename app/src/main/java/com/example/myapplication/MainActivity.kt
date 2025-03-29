package com.example.messageapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

interface ApiService {
    @GET("posts")
    fun getPosts(): Call<List<Post>>

    @POST("posts")
    fun createPost(@Body post: Post): Call<Post>
}

class PostAdapter(private val posts: List<Post>, private val listener: (Post) -> Unit) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val idTextView: TextView = view.findViewById(R.id.idTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.titleTextView.text = post.title
        holder.idTextView.text = post.id.toString()
        holder.itemView.setOnClickListener { listener(post) }
    }

    override fun getItemCount() = posts.size
}

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val posts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        postAdapter = PostAdapter(posts) { post ->
            val intent = Intent(this, EditPostActivity::class.java)
            intent.putExtra("post", post)
            startActivity(intent)
        }
        recyclerView.adapter = postAdapter

        fetchPosts()
    }

    private fun fetchPosts() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        apiService.getPosts().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        posts.clear()
                        posts.addAll(it)
                        postAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
            }
        })
    }
}

class EditPostActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var bodyEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        titleEditText = findViewById(R.id.titleEditText)
        bodyEditText = findViewById(R.id.bodyEditText)
        saveButton = findViewById(R.id.saveButton)

        post = intent.getSerializableExtra("post") as Post
        titleEditText.setText(post.title)
        bodyEditText.setText(post.body)

        saveButton.setOnClickListener {
            val updatedPost = post.copy(title = titleEditText.text.toString(), body = bodyEditText.text.toString())
            savePost(updatedPost)
        }
    }

    private fun savePost(post: Post) {
        val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)
    apiService.createPost(post).enqueue(object : Callback<Post> {
        override fun onResponse(call: Call<Post>, response: Response<Post>) {
            if (response.isSuccessful) {
                finish()
            }
        }

        override fun onFailure(call: Call<Post>, t: Throwable) {
        }
    })
}
}

