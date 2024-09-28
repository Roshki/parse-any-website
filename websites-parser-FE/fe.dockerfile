FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm install -g @angular/cli

RUN npm install
EXPOSE 4200
CMD ["ng", "serve", "--host", "0.0.0.0", "--configuration", "production"]
