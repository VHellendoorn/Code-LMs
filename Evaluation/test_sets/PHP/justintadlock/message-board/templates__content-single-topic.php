<header class="mb-page-header">

	<h1 class="mb-page-header"><?php mb_single_topic_title(); ?></h1>

	<p>
		<?php mb_topic_forum_link(); ?>
		<span class="mb-topic-post-count"><?php printf( __( 'Posts: %s', 'message-board' ), mb_get_topic_post_count() ); ?></span>
		<span class="mb-topic-voice-count"><?php printf( __( 'Participating: %s', 'message-board' ), mb_get_topic_voice_count() ); ?></span>
		<?php mb_topic_subscribe_link(); ?>
		<?php mb_topic_bookmark_link(); ?>
	</p>

</header><!-- .mb-page-header -->

<?php if ( current_user_can( 'read_topic', mb_get_topic_id() ) ) : ?>

	<ol id="mb-thread" class="mb-thread">

		<?php if ( mb_show_lead_topic() && mb_topic_query() ) : ?>

			<?php while ( mb_topic_query() ) : ?>

				<?php mb_the_topic(); ?>

				<?php mb_get_template_part( 'thread', 'topic' ); ?>

			<?php endwhile; ?>

		<?php endif; ?>

		<?php if ( mb_reply_query() ) : ?>

			<?php while ( mb_reply_query() ) : ?>

				<?php mb_the_reply(); ?>

				<?php mb_get_template_part( 'thread', 'reply' ); ?>

			<?php endwhile; ?>

		<?php endif; ?>

	</ol><!-- #thread -->

	<?php mb_single_topic_pagination(); ?>

<?php endif; ?>

<?php mb_get_template_part( 'form-reply', 'new' ); // Loads the topic reply form. ?>