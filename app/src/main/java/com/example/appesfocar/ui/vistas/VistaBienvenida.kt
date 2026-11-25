package com.example.appesfocar.ui.vistas

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.R
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.ui.theme.VerdeInstitucional
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class PilarDestacado(
    val icono: ImageVector,
    val titulo: String,
    val descripcion: String
)

@Composable
fun VistaBienvenida(
    onIniciarSesion: () -> Unit,
    onRegistrarse: () -> Unit
) {
    var mostrarAcceso by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(4000)
        mostrarAcceso = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A2E0E),
                        VerdeInstitucional,
                        Color(0xFF2E7D32)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.85f, size.height * 0.15f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = size.width * 0.75f,
                center = Offset(size.width * 0.1f, size.height * 0.9f)
            )
        }

        AnimatedContent(
            targetState = mostrarAcceso,
            transitionSpec = {
                (fadeIn(tween(600)) + scaleIn(initialScale = 0.94f, animationSpec = tween(600)))
                    .togetherWith(fadeOut(tween(450)))
            },
            label = "transicion_bienvenida"
        ) { esPantallaAcceso ->
            if (!esPantallaAcceso) {
                PantallaSplashInstitucional()
            } else {
                PantallaAccesoSistema(
                    onIniciarSesion = onIniciarSesion,
                    onRegistrarse = onRegistrarse
                )
            }
        }
    }
}

@Composable
private fun PantallaSplashInstitucional() {
    val scaleAnim = remember { Animatable(0.75f) }
    val alphaAnim = remember { Animatable(0f) }
    var iniciarBarra by remember { mutableStateOf(false) }

    val progresoBarra by animateFloatAsState(
        targetValue = if (iniciarBarra) 1f else 0f,
        animationSpec = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
        label = "progreso_splash"
    )

    LaunchedEffect(Unit) {
        iniciarBarra = true
        alphaAnim.animateTo(1f, animationSpec = tween(550))
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp)
            .alpha(alphaAnim.value)
            .scale(scaleAnim.value),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 14.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_esfocar),
                    contentDescription = "Logo oficial ESFOCAR",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "ESFOCAR",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 4.sp
        )

        Text(
            text = "Plataforma Digital de Evaluación\ny Seguimiento Académico",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        LinearProgressIndicator(
            progress = { progresoBarra },
            modifier = Modifier
                .width(180.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = Color(0xFFA5D6A7),
            trackColor = Color.White.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )

        Text(
            text = "Iniciando plataforma...",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun PantallaAccesoSistema(
    onIniciarSesion: () -> Unit,
    onRegistrarse: () -> Unit
) {
    val pilares = remember {
        listOf(
            PilarDestacado(
                icono = Icons.Default.AssignmentTurnedIn,
                titulo = "Rúbricas Digitales",
                descripcion = "Evaluación estructurada por criterios y cálculo automático."
            ),
            PilarDestacado(
                icono = Icons.Default.Insights,
                titulo = "Seguimiento en Tiempo Real",
                descripcion = "Consulta inmediata de calificaciones y retroalimentación."
            ),
            PilarDestacado(
                icono = Icons.Default.CloudDone,
                titulo = "Respaldo Institucional",
                descripcion = "Sincronización segura entre aula y nube académica."
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pilares.size })
    val scope = rememberCoroutineScope()

    // Desplazamiento automático fluido con HorizontalPager
    LaunchedEffect(pagerState) {
        while (true) {
            delay(2200)
            val siguiente = (pagerState.currentPage + 1) % pilares.size
            pagerState.animateScrollToPage(
                page = siguiente,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_esfocar),
                    contentDescription = "Logo oficial ESFOCAR",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ESFOCAR",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 3.sp
            )
        }

        // Carrusel real deslizable (HorizontalPager) + auto-scroll
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                    scope.launch {
                        val siguiente = (pagerState.currentPage + 1) % pilares.size
                        pagerState.animateScrollToPage(siguiente)
                    }
                },
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val pilar = pilares[page]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = pilar.icono,
                                    contentDescription = null,
                                    tint = Color(0xFFA5D6A7),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pilar.titulo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = pilar.descripcion,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pilares.forEachIndexed { index, _ ->
                        val activo = index == pagerState.currentPage
                        val anchoPunto by animateDpAsState(
                            targetValue = if (activo) 20.dp else 6.dp,
                            animationSpec = tween(300),
                            label = "indicador_pilar"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(anchoPunto)
                                .clip(CircleShape)
                                .background(
                                    if (activo) Color(0xFFA5D6A7) else Color.White.copy(alpha = 0.35f)
                                )
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sistema Evaluativo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = VerdeInstitucional
                )

                Text(
                    text = "Gestión Institucional de Instructores, Docentes y Estudiantes",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ChipRolBienvenida(icono = Icons.Default.VerifiedUser, texto = "Docentes")
                    ChipRolBienvenida(icono = Icons.Default.School, texto = "Estudiantes")
                }

                Button(
                    onClick = onIniciarSesion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                ) {
                    Text(
                        text = "INICIAR SESIÓN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onRegistrarse,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeInstitucional)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = VerdeInstitucional
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "¿No tienes cuenta? Regístrate",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Text(
            text = "República de Chile • Carabineros de Chile",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.78f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
private fun ChipRolBienvenida(
    icono: ImageVector,
    texto: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = VerdeInstitucional,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = texto,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = VerdeInstitucional
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaBienvenida() {
    AppEsfocarTheme {
        VistaBienvenida(
            onIniciarSesion = {},
            onRegistrarse = {}
        )
    }
}