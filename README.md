# Explore with me
## Описание
Соцсеть, основанная на совместных походах на мероприятия. Можно создать свое место и событие, а затем набрать компанию для участия. Сервис сберегает время, потому что не приходится всем лично писать, звать, объянять и договариваться. 
Возможный аналог - мероприятия VK. 

## Стек
Docker 2.2, Java 17, Maven 4.0.0, PostgreSQL 14 alpine, Spring Boot 2.7.2, RestTemplate, JUnit 

  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original-wordmark.svg" title="Spring" alt="Spring" width="30" height="30"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original-wordmark.svg" title="postgresql" alt="postgresql" width="30" height="30"/>&nbsp;
  <img src="https://voyager.postman.com/logo/postman-logo-orange-stacked.svg" title="postman" alt="postman" width="40" height="30"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original-wordmark.svg" title="docker" alt="docker" width="30" height="30"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original-wordmark.svg" title="java" alt="java" width="30" height="30"/>&nbsp;
  <img src="https://raw.githubusercontent.com/junit-team/junit5/86465f4f491219ad0c0cf9c64eddca7b0edeb86f/assets/img/junit5-logo.svg" title="junit" alt="junit" width="30" height="30"/>&nbsp;
  <img src="https://upload.wikimedia.org/wikipedia/commons/b/b0/NewTux.svg" title="linux" alt="linux" width="25" height="30"/>&nbsp;
  <img src="https://www.svgrepo.com/show/373829/maven.svg" title="maven" alt="maven" width="25" height="40"/>&nbsp;

### Подробнее

- микросервисное приложение. Состоит из двух сервисов: основного - main_server со всей бизнес логикой, и статистики stats_server, собирающей статистику просмотров событий. У каждого своя БД PostgreSql.

- у основного сервиса три API взаимодействия: публичная, приватная (для авторизированных пользователей) и административная

- к событиям реализованы комментарии, их может оставлять только аккаунт посетивший мероприятие

- в публичном API просматриваются только одобренные администратором комментарии

- авторизированные пользователи могут создавать события, подтверждать (при необходимости) участников события, участвовать в них, оставлять комментарии


## Установка

```bash

```

## Как использовать

```bash

```

## Участие в проекте

Если хочется и нужна практика - не стесняйся, планов много.

### Планы по улучшению
- [ ] покрыть тестами
- [ ] написать документацию где необходимо
- [ ] заполнить раздел с инструкцией по установке и использованию
- [ ] разделить/добавить свои логи на info, trace, debug
- [ ] открутить прект от workflow яндекса и перенести в свой репозиторий, чтобы вдруг яндекс там ничего не обновил и приложение не перестало проходить проверки.
### Планы по возможному расширению
- [ ] прикрутить какой-нибудь интерфейс
- [ ] добавить сервис аутентификации на spring security
- [ ] добавить возможность сохранения фотографий
- [ ] подумать о добавлении брокера сообщений 
- [ ] запаковать все в .exe так, чтобы кто угодно смог установить и потыкать
