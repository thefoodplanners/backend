CREATE TABLE "users" (
  "id" SERIAL PRIMARY KEY,
  "email" varchar NOT NULL,
  "username" varchar UNIQUE NOT NULL,
  "password" varchar NOT NULL,
  "target_calories" integer,
  "dietary_requirements" text[] NOT NULL
);

CREATE TABLE "recipes" (
  "id" SERIAL PRIMARY KEY,
  "name" varchar NOT NULL,
  "meal_type" varchar NOT NULL,
  "description" text,
  "image_url" varchar,
  "calories" integer NOT NULL,
  "carbohydrates" float NOT NULL,
  "proteins" float NOT NULL,
  "fats" float NOT NULL,
  "dietary_requirements" text[] NOT NULL
);

CREATE TABLE "meals" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer REFERENCES "users" (id),
  "date" date NOT NULL,
  "meal_number" integer NOT NULL,
  "recipe_id" integer REFERENCES "recipes" (id)
);
