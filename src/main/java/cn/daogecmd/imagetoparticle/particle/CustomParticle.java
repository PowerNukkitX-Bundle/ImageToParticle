package cn.daogecmd.imagetoparticle.particle;

import cn.nukkit.math.Vector3;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import lombok.Builder;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.function.Supplier;

public final class CustomParticle {
    public static final Gson GSON;
    private static final String VAR_TYPE_ARRAY = "member_array";
    private static final String VAR_TYPE_FLOAT = "float";
    private static final String VAR_BASE = "variable.";
    private static final String VAR_COLOR = VAR_BASE + "color";
    private static final String VAR_MOTION = VAR_BASE + "m_";
    private static final String VAR_MOTION_X = VAR_MOTION + "x";
    private static final String VAR_MOTION_Y = VAR_MOTION + "y";
    private static final String VAR_MOTION_Z = VAR_MOTION + "z";
    private static final String VAR_SPEED = VAR_BASE + "speed";
    private static final String VAR_ACCELE = VAR_BASE + "accele";
    private static final String VAR_SIZE = VAR_BASE + "size";
    private static final String VAR_LIFE = VAR_BASE + "life";
    private static final int RGB_MAX = 255;
    private static final int VECTOR_MAX_SIZE = 1;
    private static final int VECTOR_MIN_SIZE = -1;
    private static final int SPEED_MAX = 100;
    private static final int SPEED_MIN = 0;
    private static final int ACCELE_MAX = 100;
    private static final int ACCELE_MIN = -100;
    private static final int SIZE_MAX = 100;
    private static final int SIZE_CUT = 0;
    private static final int LIFE_MAX = 1000;
    private static final int LIFE_MIN = 0;

    //init GSON
    static {
        var builder = new GsonBuilder();
        //注册自定义序列化器
        builder.registerTypeAdapter(new TypeToken<CustomParticle>() {
        }.getType(), new CustomParticleSerializer());
        GSON = builder.create();
    }

    private int color_r = 0;
    private int color_g = 0;
    private int color_b = 0;

    @Builder.Default
    private Vector3 motion = new Vector3(0, 0, 0);
    @Builder.Default
    private float size = 0.075f;
    @Builder.Default
    private float life = 10;
    @Builder.Default
    private float speed = 0.0f;
    @Builder.Default
    private float accele = 0.0f;

    /**
     * @param size   0 < $size <= 100
     * @param life   0 < 1000 <= 1000
     * @param motion If null, it is automatically set to 0. Don`t exceed the value of 1 for the values x, y, and z of the Vector.
     * @param speed  0 ~ 100
     * @param accele -100 ~ 100
     * @throws IllegalArgumentException If less than the minimum or greater than the maximum
     */
    @Builder
    public CustomParticle(float size, float life, @Nullable Vector3 motion, float speed, float accele) {
        this.size = size;
        this.life = life;
        this.motion = motion;
        this.speed = speed;
        this.accele = accele;
        checkVector();
        checkSpeed();
        checkAccele();
        checkSize();
        checkLife();
    }

    /**
     * @param r 0 ~ 255
     * @param g 0 ~ 255
     * @param b 0 ~ 255
     */
    public CustomParticle setColor(int r, int g, int b) {
        color_r = r;
        color_g = g;
        color_b = b;
        return this;
    }

    public String encode() {
        return GSON.toJson(this);
    }

    private void checkVector() {
        var x = motion.getX();
        var y = motion.getY();
        var z = motion.getZ();
        if (
                x > VECTOR_MAX_SIZE ||
                        y > VECTOR_MAX_SIZE ||
                        z > VECTOR_MAX_SIZE ||
                        x < VECTOR_MIN_SIZE ||
                        y < VECTOR_MIN_SIZE ||
                        z < VECTOR_MIN_SIZE
        ) {
            throw new IllegalArgumentException("Vector size must be between " + VECTOR_MAX_SIZE + " and " + VECTOR_MIN_SIZE);
        }
    }

    private void checkSpeed() {
        if (speed > SPEED_MAX || speed < SPEED_MIN) {
            throw new IllegalArgumentException("speed must be between " + SPEED_MIN + " and " + SPEED_MAX);
        }
    }

    private void checkAccele() {
        if (accele > ACCELE_MAX || accele < ACCELE_MIN) {
            throw new IllegalArgumentException("accele must be between " + ACCELE_MIN + " and " + ACCELE_MAX);
        }
    }

    private void checkSize() {
        if (size > SIZE_MAX || size <= SIZE_CUT) {
            throw new IllegalArgumentException("size must be greater than " + SIZE_CUT + " and less than or equal to " + SIZE_MAX);
        }
    }

    private void checkLife() {
        if (life > LIFE_MAX || life < LIFE_MIN) {
            throw new IllegalArgumentException("accele must be between " + LIFE_MIN + " and " + LIFE_MAX);
        }
    }

    public static class CustomParticleSerializer implements JsonSerializer<CustomParticle> {
        @Override
        public JsonElement serialize(CustomParticle customParticle, Type type, JsonSerializationContext jsonSerializationContext) {
            var array = new JsonArray();

            array.add(generateVarColor(customParticle));
            array.add(generateSimpleValueObject(VAR_MOTION_X, VAR_TYPE_FLOAT, () -> customParticle.motion.getX()));
            array.add(generateSimpleValueObject(VAR_MOTION_Y, VAR_TYPE_FLOAT, () -> customParticle.motion.getY()));
            array.add(generateSimpleValueObject(VAR_MOTION_Z, VAR_TYPE_FLOAT, () -> customParticle.motion.getZ()));
            array.add(generateSimpleValueObject(VAR_SPEED, VAR_TYPE_FLOAT, () -> customParticle.speed / SPEED_MAX));
            array.add(generateSimpleValueObject(VAR_ACCELE, VAR_TYPE_FLOAT, () -> customParticle.accele / ACCELE_MAX));
            array.add(generateSimpleValueObject(VAR_SIZE, VAR_TYPE_FLOAT, () -> customParticle.size / SIZE_MAX));
            array.add(generateSimpleValueObject(VAR_LIFE, VAR_TYPE_FLOAT, () -> customParticle.life / LIFE_MAX));

            return array;
        }

        private JsonObject generateVarColor(CustomParticle customParticle) {
            var varColor = new JsonObject();
            varColor.addProperty("name", VAR_COLOR);

            var varColorValue = new JsonObject();
            varColorValue.addProperty("type", VAR_TYPE_ARRAY);

            var rgbValue = new JsonArray();
            rgbValue.add(generateSimpleValueObject(".r", VAR_TYPE_FLOAT, () -> customParticle.color_r / (double) RGB_MAX));
            rgbValue.add(generateSimpleValueObject(".g", VAR_TYPE_FLOAT, () -> customParticle.color_g / (double) RGB_MAX));
            rgbValue.add(generateSimpleValueObject(".b", VAR_TYPE_FLOAT, () -> customParticle.color_b / (double) RGB_MAX));

            varColorValue.add("value", rgbValue);
            varColor.add("value", varColorValue);

            return varColor;
        }

        private JsonObject generateSimpleValueObject(String name, String type, Supplier<Number> value) {
            var object = new JsonObject();
            object.addProperty("name", name);

            var valueObject = new JsonObject();
            valueObject.addProperty("type", type);
            valueObject.addProperty("value", value.get());

            object.add("value", valueObject);
            return object;
        }
    }
}
