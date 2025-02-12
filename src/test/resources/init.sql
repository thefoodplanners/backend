CREATE TYPE "meal_type" AS ENUM (
  'breakfast',
  'lunch',
  'dinner'
);

CREATE TYPE "diet" AS ENUM (
  'vegan',
  'vegetarian',
  'halal',
  'kosher'
);

CREATE TABLE "users" (
  "id" SERIAL PRIMARY KEY,
  "email" varchar NOT NULL,
  "username" varchar UNIQUE NOT NULL,
  "password" varchar NOT NULL,
  "target_calories" integer,
  "dietary_requirements" diet[] NOT NULL
);

CREATE TABLE "recipes" (
  "id" SERIAL PRIMARY KEY,
  "name" varchar NOT NULL,
  "meal_type" meal_type NOT NULL,
  "description" text,
  "image_url" varchar,
  "calories" integer NOT NULL,
  "carbohydrates" float NOT NULL,
  "proteins" float NOT NULL,
  "fats" float NOT NULL,
  "dietary_requirements" diet[] NOT NULL
);

CREATE TABLE "meals" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer REFERENCES "users" (id),
  "date" date NOT NULL,
  "meal_number" integer NOT NULL,
  "recipe_id" integer REFERENCES "recipes" (id)
);
