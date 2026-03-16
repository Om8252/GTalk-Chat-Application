#CDAC-INTERNAL

docker run -d -p 6379:6379 redis:7

docker run -d -p 9000:9000 -p 9001:9001 -e "MINIO_ROOT_USER=admin" -e "MINIO_ROOT_PASSWORD=password" minio/minio server /data --console-address ":9001"

docker run -d  --name discord-mysql -p 3307:3306 -e "MYSQL_ROOT_PASSWORD=rootpassword" -e "MYSQL_USER=discord" -e "MYSQL_PASSWORD=discord" -e "MYSQL_DATABASE=discord_db" mysql:8

