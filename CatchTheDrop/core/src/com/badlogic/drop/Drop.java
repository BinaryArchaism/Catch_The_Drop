package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;


import java.util.Iterator;

public class Drop extends ApplicationAdapter {
	Texture dropImage;
	Texture bucketImage;
	Sound dropSound;
	Music rainMusic;

	OrthographicCamera camera;
	SpriteBatch batch;

	//позиция и размер
	Rectangle bucket;

	// добавляем каплю
	Array<com.badlogic.gdx.math.Rectangle> raindrops; //Array == ArrayList, но от последнего много мусора, производительность падает
	long lastDropTime;

	@Override
	public void create () {
		//загрузка
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		//воспроизведение фоновой музыки дождя
		rainMusic.setLooping(true);
		rainMusic.play();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		batch = new SpriteBatch();

		bucket = new Rectangle();
		bucket.x = 800/2 - 64/2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		//создаем капли
		raindrops = new Array<Rectangle>();
		spawnRainDrop();
	}

	@Override
	public void render () {
		// установит цвет очистки в синий цвет
		Gdx.gl.glClearColor(0,0,0.2f,1);
		//вызов говорит OpenGL очистить экран
		Gdx.gl.glClear((GL20.GL_COLOR_BUFFER_BIT));

		camera.update();
		//использовать систему координат камеры
		batch.setProjectionMatrix(camera.combined); //camera.combined есть матрница
		//записываем команды рисования SpiteBatch
		batch.begin();
			batch.draw(bucketImage, bucket.x, bucket.y);
		//прекрашаем запись
		batch.end();

		//управление мышью или нажатием(андроид)
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3(); // создаем объект тут, потому что сборщик мусора на андроид может вызвать торможение
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos); // Для преобразования этих координат в систему координат нашей камеры
			bucket.x = (int) touchPos.x - 64/2;
		}

		//управление клавиатурой
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

		//убеждаемся, что ведро в границах экрана
		if (bucket.x < 0) bucket.x = 0;
		if (bucket.x > 800-64) bucket.x = 800 - 64;


		//проверяем время
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRainDrop();

		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200* Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0) iter.remove();
			if (raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}

		batch.begin();
			batch.draw(bucketImage, bucket.x, bucket.y);
			for (Rectangle raindrop: raindrops) {
				batch.draw(dropImage, raindrop.x , raindrop.y);
			}

		batch.end();

	}

	@Override
	public void dispose () {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}

	private void spawnRainDrop() {
		Rectangle rainDrop = new Rectangle();
		rainDrop.x = MathUtils.random(0, 800-64);
		rainDrop.y = 480;
		rainDrop.width = 64;
		rainDrop.height = 64;
		raindrops.add(rainDrop);
		lastDropTime = TimeUtils.nanoTime();
	}
}
