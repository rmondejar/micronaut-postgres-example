INSERT INTO public.user_data (id, "password", user_role, username, "token") VALUES(0, 'password1', 'VIEW', 'user1', null);
INSERT INTO public.user_data (id, "password", user_role, username, "token") VALUES(1, 'password2', 'VIEW', 'user2', null);
INSERT INTO public.user_data (id, "password", user_role, username, "token") VALUES(2, 'password3', 'VIEW', 'user3', null);

INSERT INTO public.message (id, "content", creation_date, user_ref) VALUES(0, 'My name is user1', '2020-06-07 21:03:41.889', 0);
INSERT INTO public.message (id, "content", creation_date, user_ref) VALUES(1, 'My name is user2', '2020-06-07 21:03:41.889', 1);
INSERT INTO public.message (id, "content", creation_date, user_ref) VALUES(2, 'My name is user3', '2020-06-07 21:03:41.889', 2);