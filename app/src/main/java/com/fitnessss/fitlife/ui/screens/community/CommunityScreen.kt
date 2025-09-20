package com.fitnessss.fitlife.ui.screens.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitnessss.fitlife.navigation.Screen
import com.fitnessss.fitlife.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Feed", "Challenges", "Forums", "Partners")
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Community",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { /* TODO: Create post */ }) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Post")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> CommunityFeed(navController = navController)
            1 -> ChallengesSection(navController = navController)
            2 -> ForumsSection(navController = navController)
            3 -> WorkoutPartnersSection(navController = navController)
        }
    }
}

@Composable
fun CommunityFeed(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(getSamplePosts()) { post ->
            PostCard(post = post)
        }
    }
}

@Composable
fun PostCard(post: SamplePost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture
                Card(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = post.userColor.copy(alpha = 0.2f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = post.userColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.userName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                IconButton(onClick = { /* TODO: More options */ }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (post.workoutInfo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = WorkoutPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.SportsGymnastics,
                            contentDescription = "Workout",
                            tint = WorkoutPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = post.workoutInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = WorkoutPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Post Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PostAction(
                        icon = Icons.Filled.Favorite,
                        text = "${post.likes}",
                        onClick = { /* TODO: Like post */ }
                    )
                    
                    PostAction(
                        icon = Icons.Filled.Chat,
                        text = "${post.comments}",
                        onClick = { /* TODO: Comment */ }
                    )
                    
                    PostAction(
                        icon = Icons.Filled.Share,
                        text = "Share",
                        onClick = { /* TODO: Share post */ }
                    )
                }
            }
        }
    }
}

@Composable
fun PostAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text)
    }
}

@Composable
fun ChallengesSection(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(getSampleChallenges()) { challenge ->
            ChallengeCard(challenge = challenge)
        }
    }
}

@Composable
fun ChallengeCard(challenge: SampleChallenge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = challenge.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${challenge.participants} participants",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${challenge.daysLeft} days left",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Button(
                    onClick = { /* TODO: Join challenge */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CommunityPrimary
                    )
                ) {
                    Text("Join Challenge")
                }
            }
        }
    }
}

@Composable
fun ForumsSection(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(getSampleForums()) { forum ->
            ForumCard(forum = forum)
        }
    }
}

@Composable
fun ForumCard(forum: SampleForum) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = { /* TODO: Navigate to forum */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = forum.icon,
                contentDescription = forum.name,
                tint = forum.color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = forum.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${forum.memberCount} members • ${forum.postCount} posts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Navigate")
        }
    }
}

@Composable
fun WorkoutPartnersSection(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(getSamplePartners()) { partner ->
            PartnerCard(partner = partner)
        }
    }
}

@Composable
fun PartnerCard(partner: SamplePartner) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Card(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(
                    containerColor = partner.color.copy(alpha = 0.2f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = partner.color
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partner.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = partner.fitnessLevel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = partner.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Button(
                onClick = { /* TODO: Connect with partner */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CommunityPrimary
                )
            ) {
                Text("Connect")
            }
        }
    }
}

// Data classes for sample data
data class SamplePost(
    val userName: String,
    val userColor: Color,
    val content: String,
    val workoutInfo: String?,
    val likes: Int,
    val comments: Int,
    val timeAgo: String
)

data class SampleChallenge(
    val name: String,
    val description: String,
    val participants: Int,
    val daysLeft: Int
)

data class SampleForum(
    val name: String,
    val memberCount: Int,
    val postCount: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

data class SamplePartner(
    val name: String,
    val fitnessLevel: String,
    val location: String,
    val color: Color
)

fun getSamplePosts(): List<SamplePost> {
    return listOf(
        SamplePost(
            userName = "Sarah Johnson",
            userColor = WorkoutPrimary,
            content = "Just completed an amazing upper body workout! Feeling stronger every day 💪",
            workoutInfo = "Upper Body Strength • 45 min • 320 cal",
            likes = 24,
            comments = 8,
            timeAgo = "2 hours ago"
        ),
        SamplePost(
            userName = "Mike Chen",
            userColor = ProgressPrimary,
            content = "Hit a new personal record on deadlifts today! 275 lbs for 3 reps. Progress feels amazing!",
            workoutInfo = null,
            likes = 31,
            comments = 12,
            timeAgo = "4 hours ago"
        ),
        SamplePost(
            userName = "Emma Davis",
            userColor = NutritionPrimary,
            content = "Meal prep Sunday! Ready for a healthy week ahead 🥗",
            workoutInfo = null,
            likes = 18,
            comments = 5,
            timeAgo = "6 hours ago"
        )
    )
}

fun getSampleChallenges(): List<SampleChallenge> {
    return listOf(
        SampleChallenge(
            name = "30-Day Push-up Challenge",
            description = "Build upper body strength with daily push-ups",
            participants = 156,
            daysLeft = 15
        ),
        SampleChallenge(
            name = "Summer Body Transformation",
            description = "12-week program to get beach ready",
            participants = 89,
            daysLeft = 42
        ),
        SampleChallenge(
            name = "Running Streak",
            description = "Run at least 1 mile every day",
            participants = 203,
            daysLeft = 7
        )
    )
}

fun getSampleForums(): List<SampleForum> {
    return listOf(
        SampleForum(
            name = "General Fitness",
            memberCount = 1247,
            postCount = 3421,
            icon = Icons.Filled.SportsGymnastics,
            color = WorkoutPrimary
        ),
        SampleForum(
            name = "Nutrition & Diet",
            memberCount = 892,
            postCount = 2156,
            icon = Icons.Filled.Restaurant,
            color = NutritionPrimary
        ),
        SampleForum(
            name = "Progress Tracking",
            memberCount = 567,
            postCount = 1234,
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            color = ProgressPrimary
        )
    )
}

fun getSamplePartners(): List<SamplePartner> {
    return listOf(
        SamplePartner(
            name = "Alex Thompson",
            fitnessLevel = "Intermediate",
            location = "Downtown",
            color = WorkoutPrimary
        ),
        SamplePartner(
            name = "Lisa Rodriguez",
            fitnessLevel = "Advanced",
            location = "Westside",
            color = ProgressPrimary
        ),
        SamplePartner(
            name = "David Kim",
            fitnessLevel = "Beginner",
            location = "North Park",
            color = NutritionPrimary
        )
    )
}
