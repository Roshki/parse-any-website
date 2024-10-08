FROM node:latest AS build

WORKDIR /usr/local/app

COPY ./ /usr/local/app/

RUN npm install

RUN npm run build --configuration=production

# Stage 2: Serve app with nginx server

FROM nginx:latest

COPY --from=build /usr/local/app/dist/parser/browser /usr/share/nginx/html

# COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir /etc/nginx/templates
COPY default.conf.template /etc/nginx/templates

EXPOSE 80   