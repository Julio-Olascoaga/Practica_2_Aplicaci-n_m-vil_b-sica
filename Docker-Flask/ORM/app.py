import os
from datetime import timedelta

from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_jwt_extended import (
    JWTManager,
    create_access_token,
    jwt_required,
    get_jwt_identity,
)

app = Flask(__name__)

# 1. Configuracion de la Base de Datos (SQLite)
# El archivo se guarda en la carpeta del contenedor como 'site.db'
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///site.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# 2. Configuracion de JWT (sesiones seguras)
# La llave secreta se toma de una variable de entorno; nunca debe quedar
# escrita en el codigo ni subirse al repositorio (ver .env.example).
app.config['JWT_SECRET_KEY'] = os.environ.get('JWT_SECRET_KEY', 'dev-secret-change-me')
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = timedelta(hours=int(os.environ.get('JWT_EXPIRES_HOURS', 2)))

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
jwt = JWTManager(app)


# 3. Modelos

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(20), unique=True, nullable=False)
    password = db.Column(db.String(60), nullable=False)  # hash de bcrypt

    def __repr__(self):
        return f"User('{self.username}')"


class Tarea(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    titulo = db.Column(db.String(120), nullable=False)
    descripcion = db.Column(db.String(500), nullable=True)
    completada = db.Column(db.Boolean, default=False, nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'titulo': self.titulo,
            'descripcion': self.descripcion,
            'completada': self.completada,
        }


# 4. Ruta de verificacion

@app.route('/')
def hello():
    return jsonify({"message": "API Funcionando"})


# 5. Autenticacion

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json(silent=True) or {}
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({"message": "username y password son obligatorios"}), 400

    if User.query.filter_by(username=username).first():
        return jsonify({"message": "El usuario ya existe"}), 400

    hashed_password = bcrypt.generate_password_hash(password).decode('utf-8')

    new_user = User(username=username, password=hashed_password)
    db.session.add(new_user)
    db.session.commit()

    return jsonify({"message": "Usuario creado exitosamente"}), 201


@app.route('/login', methods=['POST'])
def login():
    data = request.get_json(silent=True) or {}
    username = data.get('username')
    password = data.get('password')

    user = User.query.filter_by(username=username).first()

    if user and bcrypt.check_password_hash(user.password, password):
        access_token = create_access_token(identity=str(user.id))
        return jsonify({
            "status": "success",
            "message": "Login exitoso",
            "access_token": access_token,
            "user_id": user.id,
            "username": user.username
        }), 200
    else:
        return jsonify({"status": "error", "message": "Credenciales invalidas"}), 401


# 6. CRUD de Tareas (protegido con JWT)

@app.route('/tareas', methods=['GET'])
@jwt_required()
def listar_tareas():
    user_id = int(get_jwt_identity())
    tareas = Tarea.query.filter_by(user_id=user_id).all()
    return jsonify([t.to_dict() for t in tareas]), 200


@app.route('/tareas/<int:tarea_id>', methods=['GET'])
@jwt_required()
def obtener_tarea(tarea_id):
    user_id = int(get_jwt_identity())
    tarea = Tarea.query.filter_by(id=tarea_id, user_id=user_id).first()
    if not tarea:
        return jsonify({"message": "Tarea no encontrada"}), 404
    return jsonify(tarea.to_dict()), 200


@app.route('/tareas', methods=['POST'])
@jwt_required()
def crear_tarea():
    user_id = int(get_jwt_identity())
    data = request.get_json(silent=True) or {}
    titulo = data.get('titulo')

    if not titulo:
        return jsonify({"message": "El campo 'titulo' es obligatorio"}), 400

    tarea = Tarea(
        titulo=titulo,
        descripcion=data.get('descripcion', ''),
        completada=bool(data.get('completada', False)),
        user_id=user_id,
    )
    db.session.add(tarea)
    db.session.commit()
    return jsonify(tarea.to_dict()), 201


@app.route('/tareas/<int:tarea_id>', methods=['PUT'])
@jwt_required()
def actualizar_tarea(tarea_id):
    user_id = int(get_jwt_identity())
    tarea = Tarea.query.filter_by(id=tarea_id, user_id=user_id).first()
    if not tarea:
        return jsonify({"message": "Tarea no encontrada"}), 404

    data = request.get_json(silent=True) or {}
    if 'titulo' in data:
        if not data['titulo']:
            return jsonify({"message": "El campo 'titulo' no puede estar vacio"}), 400
        tarea.titulo = data['titulo']
    if 'descripcion' in data:
        tarea.descripcion = data['descripcion']
    if 'completada' in data:
        tarea.completada = bool(data['completada'])

    db.session.commit()
    return jsonify(tarea.to_dict()), 200


@app.route('/tareas/<int:tarea_id>', methods=['DELETE'])
@jwt_required()
def borrar_tarea(tarea_id):
    user_id = int(get_jwt_identity())
    tarea = Tarea.query.filter_by(id=tarea_id, user_id=user_id).first()
    if not tarea:
        return jsonify({"message": "Tarea no encontrada"}), 404

    db.session.delete(tarea)
    db.session.commit()
    return jsonify({"message": "Tarea eliminada"}), 200


# 7. Manejadores de error de JWT -> siempre responder 401
@jwt.unauthorized_loader
def unauthorized_callback(err_msg):
    return jsonify({"status": "error", "message": "Se requiere iniciar sesion"}), 401


@jwt.invalid_token_loader
def invalid_token_callback(err_msg):
    return jsonify({"status": "error", "message": "Sesion invalida o expirada"}), 401


@jwt.expired_token_loader
def expired_token_callback(jwt_header, jwt_payload):
    return jsonify({"status": "error", "message": "Sesion expirada, vuelve a iniciar sesion"}), 401


if __name__ == '__main__':
    with app.app_context():
        db.create_all()

    app.run(host='0.0.0.0', port=5000, debug=True)
