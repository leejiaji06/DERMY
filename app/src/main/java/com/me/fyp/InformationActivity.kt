package com.me.fyp

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.alpha


class InformationActivity: ComponentActivity() {

    private val uriState = MutableStateFlow("")
    private val imagePicker = registerForActivityResult<PickVisualMediaRequest, Uri>(
            ActivityResultContracts.PickVisualMedia()
    ){uri->
        uri?.let {
            uriState.update{uri.toString()}
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GeminiChatBotTheme {
                Surface (
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ){
                    Scaffold (
                            topBar = {
                                Box(
                                        modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFBA6B57))
                                                .height(55.dp)
                                                .padding(horizontal = 16.dp)

                                ){
                                    Text(
                                            modifier = Modifier
                                                    .align(Alignment.CenterStart),
                                            text = "Chat Bot",
                                            fontSize = 19.sp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                    ){
                        ChatScreen(paddingValues = it)
                    }
                }


            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {
        val chaViewModel = viewModel<ChatViewModel>()
        val chatState = chaViewModel.chatState.collectAsState().value

        val bitmap = getBitmap()

        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                verticalArrangement = Arrangement.Bottom
        ) {
            LazyColumn(
                    modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    reverseLayout = true
            ) {
                if (chatState.isLoading) {
                    item { TypingIndicatorItem() }
                }
                itemsIndexed(chatState.chatList) { index, chat ->
                    if (chat.isFromUser) {
                        UserChatItem(
                                prompt = chat.prompt, bitmap = chat.bitmap
                        )
                    } else {
                        ModelChatItem(response = chat.prompt)
                    }
                }
            }

            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    bitmap?.let {
                        Image(
                                modifier = Modifier
                                        .size(40.dp)
                                        .padding(bottom = 2.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                contentDescription = "picked image",
                                contentScale = ContentScale.Crop,
                                bitmap = it.asImageBitmap()
                        )
                    }

                    Icon(
                            modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        imagePicker.launch(
                                                PickVisualMediaRequest
                                                        .Builder()
                                                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                        .build()
                                        )
                                    },
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Add Photo",
                            tint = Color(0xFFBA6B57)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                        modifier = Modifier
                                .weight(1f),
                        value = chatState.prompt,
                        onValueChange = {
                            chaViewModel.onEvent(ChatUiEvent.UpdatePrompt(it))
                        },
                        placeholder = {
                            Text(text = "Type a prompt")
                        }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                        modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    chaViewModel.onEvent(ChatUiEvent.SendPrompt(chatState.prompt, bitmap))
                                    uriState.update { "" }
                                },
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send prompt",
                        tint = Color(0xFFBA6B57)
                )

            }

        }

    }

    @Composable
    fun UserChatItem(prompt: String, bitmap: Bitmap?) {
        Column(
                modifier = Modifier.padding(start = 100.dp, bottom = 16.dp)
        ) {

            bitmap?.let {
                Image(
                        modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .padding(bottom = 2.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        contentDescription = "image",
                        contentScale = ContentScale.Crop,
                        bitmap = it.asImageBitmap()
                )
            }

            Text(
                    modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFBA6B57))
                            .padding(16.dp),
                    text = prompt,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimary
            )

        }
    }

    @Composable
    fun ModelChatItem(response: String) {
        Column(
                modifier = Modifier.padding(end = 100.dp, bottom = 16.dp)
        ) {
            Text(
                    modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDEA057))
                            .padding(16.dp),
                    text = response,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimary
            )

        }
    }

    @Composable
    fun TypingIndicatorItem() {
        val transition = rememberInfiniteTransition()
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing)
            )
        )

        Row(
            modifier = Modifier
                .padding(end = 100.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFDEA057))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(if (phase.toInt() == i) 1f else 0.35f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }

    @Composable
    private fun getBitmap(): Bitmap? {
        val uri = uriState.collectAsState().value

        val imageState: AsyncImagePainter.State = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .size(1024)              // longest side max 1024 px
                .allowHardware(false)    // readable bitmap for Gemini
                .build()
        ).state

        if (imageState is AsyncImagePainter.State.Success) {
            return imageState.result.drawable.toBitmap()
        }

        return null
    }
}