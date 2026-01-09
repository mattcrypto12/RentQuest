package com.rentquest.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rentquest.app.domain.model.Achievement
import com.rentquest.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Loot animation shown when SOL is reclaimed
 */
@Composable
fun LootAnimation(
    solAmount: Double,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    
    // Auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        isVisible = false
        onDismiss()
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "loot_scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // Sparkle particles
        SparkleParticles()
        
        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            // Coin emoji with bounce
            Text(
                text = "💰",
                fontSize = 80.sp,
                modifier = Modifier.bounceAnimation()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "+${String.format("%.6f", solAmount)} SOL",
                style = MaterialTheme.typography.displaySmall,
                color = SolanaGreen,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Reclaimed!",
                style = MaterialTheme.typography.titleLarge,
                color = Gray300
            )
        }
    }
}

/**
 * Achievement unlock animation
 */
@Composable
fun AchievementUnlockAnimation(
    achievement: Achievement,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(4000)
        isVisible = false
        onDismiss()
    }
    
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else -200f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "achievement_slide"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .offset(y = slideOffset.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceDark
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "🎉 Achievement Unlocked!",
                    style = MaterialTheme.typography.titleMedium,
                    color = WarningAmber
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = achievement.emoji,
                    fontSize = 64.sp,
                    modifier = Modifier.bounceAnimation()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = achievement.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gray50,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray400,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Continue", color = Purple400)
                }
            }
        }
    }
}

/**
 * Sparkle particles effect
 */
@Composable
fun SparkleParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 30
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 8f + 2f,
                speed = Random.nextFloat() * 0.01f + 0.005f,
                color = listOf(SolanaGreen, SolanaPurple, WarningAmber, Purple400).random()
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val animatedTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val yOffset = (particle.y + animatedTime * particle.speed * 100) % 1f
            drawCircle(
                color = particle.color.copy(alpha = 0.7f),
                radius = particle.size,
                center = Offset(
                    x = particle.x * size.width,
                    y = yOffset * size.height
                )
            )
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val color: Color
)

/**
 * Bounce animation modifier
 */
@Composable
fun Modifier.bounceAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_scale"
    )
    return this.scale(scale)
}
