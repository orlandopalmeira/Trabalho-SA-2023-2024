import io
from flask import Flask, jsonify, request, render_template, session, make_response, redirect
from flask_cors import CORS
import firebase_admin
from firebase_admin import credentials, firestore
from google.cloud import storage
from collections import defaultdict
from datetime import datetime
import datetime as dt
import jwt
from functools import wraps
import random
import csv
import threading
import time
import math

def verifica_token_return_admin_name(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'token' not in session:
            return redirect('/login')

        try:
            token_data = jwt.decode(session['token'], app.secret_key, algorithms=['HS256'])
            # Verifica se o utilizador do token está presente nos utilizadores armazenados
            if token_data['username'] not in users:
                return jsonify({'message': 'Utilizador não autorizado'}), 401

            return f(token_data['username'], *args, **kwargs)
        except jwt.ExpiredSignatureError as e:
            print(e)
            print('Token expirado')
            return redirect('/login')
        except jwt.InvalidTokenError as e:
            print(e)
            print('Token inválido')
            return redirect('/login')

    return decorated_function

def verifica_token(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'token' not in session:
            return jsonify({'message': 'Não autorizado'}), 401

        try:
            token_data = jwt.decode(session['token'], app.secret_key, algorithms=['HS256'])
            # Verifica se o utilizador do token está presente nos utilizadores armazenados
            if token_data['username'] not in users:
                return jsonify({'message': 'Utilizador não autorizado'}), 401

            return f(*args, **kwargs)
        except jwt.ExpiredSignatureError as e:
            print(e)
            print('Token expirado')
            return jsonify({'message': 'Token expirado'}), 401
        except jwt.InvalidTokenError as e:
            print(e)
            print('Token inválido')
            return jsonify({'message': 'Token inválido'}), 401

    return decorated_function

def dia_da_semana(data_str):
    # Converter a string para um objecto datetime
    data = datetime.strptime(data_str, '%Y-%m-%d')
    # Obter o dia da semana (0 = segunda-feira, 1 = terça-feira, ..., 6 = domingo)
    dia_semana = data.weekday()

    # Mapear o número do dia da semana para o nome do dia
    dias_da_semana = ['Segunda-feira', 'Terça-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sábado', 'Domingo']
    nome_dia = dias_da_semana[dia_semana]

    return nome_dia

def dia_da_semana_sorter(document):
    # Mapear o nome do dia para o número do dia da semana
    dias_da_semana = {
        'Segunda-feira': 0,
        'Terça-feira': 1,
        'Quarta-feira': 2,
        'Quinta-feira': 3,
        'Sexta-feira': 4,
        'Sábado': 5,
        'Domingo': 6
    }
    numero_dia = dias_da_semana[document['diaDaSemana']]

    return numero_dia

def haversine_distance(lat1, lon1, lat2, lon2):
    """
    Calcula a distância em metros entre duas posições geográficas (latitude e longitude) usando a fórmula de Haversine.
    """
    # Raio da Terra em metros
    R = 6371000.0
    
    # Converte coordenadas de graus para radianos
    lat1_rad = math.radians(lat1)
    lon1_rad = math.radians(lon1)
    lat2_rad = math.radians(lat2)
    lon2_rad = math.radians(lon2)
    
    # Diferença de latitude e longitude
    dlat = lat2_rad - lat1_rad
    dlon = lon2_rad - lon1_rad
    
    # Fórmula de Haversine
    a = math.sin(dlat / 2)**2 + math.cos(lat1_rad) * math.cos(lat2_rad) * math.sin(dlon / 2)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    distance = R * c 
    
    return distance

def calcular_velocidade(lat1, lon1, timestamp1, lat2, lon2, timestamp2, velocidade_anterior = 0):
    """
    Calcula a velocidade média em km/h entre duas posições geográficas e timestamps.
    """
  
    # Calcula a distância entre as duas posições em metros
    distancia = haversine_distance(lat1, lon1, lat2, lon2)
    
    # Calcula o intervalo de tempo em segundos
    intervalo_tempo = abs((datetime.fromisoformat(timestamp2) - datetime.fromisoformat(timestamp1)).seconds)
    
    # Calcula a velocidade média em metros por segundo
    velocidade = velocidade_anterior if intervalo_tempo <= 0 else (distancia / intervalo_tempo)*3.6
    if abs(velocidade_anterior - velocidade) > 40:
        velocidade = velocidade_anterior
    
    return velocidade


# Inicializa o aplicativo Flask
app = Flask(__name__)
app.secret_key = ''.join(list(random.choice('abcdefghijklmnopqrstuvwxyz1234567890') for _ in range(64)))
CORS(app, origins=['http://localhost:5000', "https://orlandopalmeira.pythonanywhere.com"])
# Inicializa o Firestore
cred = credentials.Certificate("./serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

users = {
    'admin': 'admin',
    'admin1': 'admin1',
    'admin2': 'admin2',
    'admin3': 'admin3',
    'admin4': 'admin4',
    'admin5': 'admin5'
}

@app.after_request
def add_csp_headers(response):
    response.headers['Content-Security-Policy'] = "frame-ancestors 'self' http://lookerstudio.google.com https://localhost:5000 https://localhost:5500 https://127.0.0.1:5000 https://127.0.0.1:5500 https://orlandopalmeira.pythonanywhere.com;"
    return response

@app.route('/refresh')
def refresh():
    try:
        refresh_google_cloud_store_files()
        return '', 200
    except Exception as e:
        return str(e), 500

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')

        # Verifica se o utilizador existe e se a passe está correcta
        if username in users and users[username] == password:
            token = jwt.encode({'username': username, 'exp': dt.datetime.utcnow() + dt.timedelta(minutes=30)}, app.secret_key)
            session['token'] = token
            return redirect('/dataviz')

        return jsonify({'message': 'Credenciais inválidas'}), 401

    elif request.method == 'GET':
        return render_template('login.html')

    return make_response('Could not verify', 401, {'WWW-Authenticate': 'Basic realm="Login Required"'})

@app.route('/logout')
def logout():
    session.pop('token', None)
    return redirect('/login')

# Rota para obter documentos da coleção "positions"
@app.route('/positions')
def get_positions():
    positions_ref = db.collection('positions')
    positions_docs = positions_ref.stream()

    positions = []
    for doc in positions_docs:
        document = doc.to_dict()
        document['timestamp'] = document['timestamp'][0:19].replace('T',' ')
        document['viagemID'] = document['viagemID'] if (document['viagemID'] is not None) and document['viagemID'] != "" else "sem viagem"
        positions.append(document)

    positions_with_speed = []
    groupby_viagemid = group_by(positions, 'viagemID')
    for positions_ in groupby_viagemid.values():
        positions_ = sorted(positions_, key=lambda x: x['timestamp'])
        positions_[0]['velocidade'] = 0
        positions_with_speed.append(positions_[0])
        velocidade_anterior = 0
        for i in range(1, len(positions_)):
            positions_[i]['velocidade'] = calcular_velocidade(
                positions_[i-1]['latitude'], positions_[i-1]['longitude'], 
                positions_[i-1]['timestamp'], 
                positions_[i]['latitude'], positions_[i]['longitude'],
                positions_[i]['timestamp'],
                velocidade_anterior
            ) if positions_[i]['tipoTrabalho'] == 'viagem' else 0
            velocidade_anterior = positions_[i]['velocidade']
            positions_with_speed.append(positions_[i])

    positions = positions_with_speed

    idTrabalhadorHashes = {}
    hash_code = 0
    for position in positions:
        if position['idTrabalhador'] not in idTrabalhadorHashes.keys():
            idTrabalhadorHashes[position['idTrabalhador']] = hash_code
            hash_code += 1
        position['idTrabalhadorHash'] = idTrabalhadorHashes[position['idTrabalhador']]

    return jsonify(positions)

def group_by(documentos, field: str, fun = lambda x: x):
    grupos = defaultdict(list)
    for documento in documentos:
        grupos[fun(documento[field])].append(documento)
    return dict(grupos)

@app.route('/work_time')
def get_work_time():
    work_time_ref = db.collection('work_time')
    work_time_docs = work_time_ref.stream()

    work_time = []
    for doc in work_time_docs:
        work_time.append(doc.to_dict())

    work_time = group_by(work_time, 'idTrabalhador')
    for idTrabalhador, registos in work_time.items():
        work_time[idTrabalhador] = group_by(registos, 'data', lambda x: x.split('T')[0])

    for idTrabalhador, registos in work_time.items():
        for data, registos in registos.items():
            work_time[idTrabalhador][data] = group_by(registos, 'tipoTrabalho')

    for idTrabalhador, registosData in work_time.items():
        for data, registosTipoTrabalho in registosData.items():
            for tipoTrabalho, registos in registosTipoTrabalho.items():
                work_time[idTrabalhador][data][tipoTrabalho] = {
                    'segundosDeTrabalho':sum([int(registo['segundosDeTrabalho']) for registo in registos]),
                    'username': registos[0]['username']
                }

    result = []
    for idTrabalhador, registosData in work_time.items():
        for data, registosTipoTrabalho in registosData.items():
            for tipoTrabalho, registo in registosTipoTrabalho.items():
                result.append({
                    'idTrabalhador': idTrabalhador,
                    'data': data,
                    'tipoTrabalho': tipoTrabalho,
                    'segundosDeTrabalho': registo['segundosDeTrabalho'],
                    'username': registo['username']
                })

    idTrabalhadorHashes = {}
    hash_code = 0
    for registo in result:
        if registo['idTrabalhador'] not in idTrabalhadorHashes.keys():
            idTrabalhadorHashes[registo['idTrabalhador']] = hash_code
            hash_code += 1
        registo['idTrabalhadorHash'] = idTrabalhadorHashes[registo['idTrabalhador']]

    result = [{**registo, **{'diaDaSemana': dia_da_semana(registo['data'])}} for registo in result]
    return jsonify(result)

@app.route('/work_time_viagens')
def work_time_viagens():
    work_time_viagens = db.collection('work_time').where('tipoTrabalho', '==', 'viagem').get()
    work_time_viagens = [doc.to_dict() for doc in work_time_viagens]
    work_time_viagens = group_by(work_time_viagens, 'idTrabalhador')

    for idTrabalhador, registos in work_time_viagens.items():
        work_time_viagens[idTrabalhador] = group_by(registos, 'data', lambda x: x.split('T')[0])

    for idTrabalhador, registos in work_time_viagens.items():
        for data, registos in registos.items():
            work_time_viagens[idTrabalhador][data] = {
                'segundosDeTrabalho': sum([int(registo['segundosDeTrabalho']) for registo in registos]),
                'username': registos[0]['username']
            }

    result = []
    for idTrabalhador, registosData in work_time_viagens.items():
        for data, registo in registosData.items():
            result.append({
                'idTrabalhador': idTrabalhador,
                'data': data,
                'segundosDeTrabalho': registo['segundosDeTrabalho'],
                'username': registo['username']
            })

    idTrabalhadorHashes = {}
    hash_code = 0
    for registo in result:
        if registo['idTrabalhador'] not in idTrabalhadorHashes.keys():
            idTrabalhadorHashes[registo['idTrabalhador']] = hash_code
            hash_code += 1
        registo['idTrabalhadorHash'] = idTrabalhadorHashes[registo['idTrabalhador']]

    result = [{**registo, **{'diaDaSemana': dia_da_semana(registo['data'])}} for registo in result]

    return jsonify(result)

# Rota para obter documentos da coleção "geofences"
@app.route('/geofences')
def get_geofences():
    geofences_ref = db.collection('geofences')
    geofences_docs = geofences_ref.stream()

    geofences = []
    for doc in geofences_docs:
        geofences.append({'id': doc.id, **doc.to_dict()})

    return jsonify(geofences)

@app.route('/geofencesmap')
def get_geofencesmap():
    return render_template('geovedacoes.html')

@app.route('/addfirebasegeofences', methods=['POST'])
@verifica_token
def addfirebasegeofences():
    geofences = request.json
    geofencesCollection = db.collection('geofences')
    for geofence in geofences:
        geofencesCollection.add(geofence)
    return '', 200

@app.route('/geofence/<id>', methods=['DELETE'])
@verifica_token
def deletegeofence(id):
    geofence_ref = db.collection('geofences').document(id)
    geofence_ref.delete()
    return '', 200

@app.route('/managegeofences')
@verifica_token_return_admin_name
def managegeofences(admin_name):
    return render_template('addremovegeofences.html', admin_name=admin_name)

@app.route('/dataviz')
@verifica_token_return_admin_name
def dataviz(username):
    return render_template('dataviz.html')

if __name__ == '__main__':
    app.run(debug=True)
