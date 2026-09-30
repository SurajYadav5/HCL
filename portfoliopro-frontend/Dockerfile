# Step 1: Build Angular application
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# Step 2: Serve application with Nginx
FROM nginx:alpine
COPY --from=build /app/dist/portfoliopro-frontend /tmp/dist
RUN if [ -d "/tmp/dist/browser" ]; then cp -r /tmp/dist/browser/* /usr/share/nginx/html/; else cp -r /tmp/dist/* /usr/share/nginx/html/; fi && rm -rf /tmp/dist

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
