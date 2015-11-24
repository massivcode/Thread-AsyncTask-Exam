# 안드로이드와 스레드 그리고 다양한 Helper 클래스

안드로이드에서 앱 하나를 실행하면 프로세스 하나가 생성된다. 

하나의 프로세스에는 최소 하나의 스레드가 존재해야 하며, 따라서 스레드가 존재하지 않는 프로세스는
종료된 프로세스라고 보면 된다. 그러므로 앱이 실행되면 프로세스와 스레드 하나가 꼭 생성된다.

안드로이드에서 앱이 실행될 때 기본으로 생성되는 스레드를 메인 스레드라고 한다. 

메인 스레드는 각종 생명주기 함수들을 처리하고 <b>화면에 그림을 그리는 등의 역할</b>을 한다.

# 메인 스레드와 ANR (Application not Responding)
개발자가 별도의 작업 스레드를 만들지 않는 이상 구현되는 모든 코드는 메인 스레드에서 동작한다.

따라서 별도의 작업 스레드를 만들지 않고 메인 스레드가 긴 작업으로 잠기게 된다면 안드로이드는 강제로
앱을 종료시키기 위한 팝업을 구동한다.

이를 앱이 응답 없음을 의미하는 <b>ANR (Application not Responding)</b> 팝업이라고 하고, 사용자는 팝업을 통해 앱을 강제로 종료할 수 있다.

<i><b>* 안드로이드 6.0에서는 ANR이 발생하지 않았음. 메인 스레드가 잠긴 동안 터치 이벤트가 발생할 경우 무시되는 것으로 추정되니 이 부분에 대해 정확한 확인 바람.</b></i>

다음의 코드는 ANR을 의도적으로 발생시키는 코드이다.

```java
for (int i = 0; i < 15; i++) {
            mCount++;

            Log.i(TAG, "Current Count : " + mCount);
            // 수행되는 동안 UI가 멈춰있다.
            // UI 스레드에서 시간이 오래 걸리는 작업을 해서는 안된다.
            // 메인 스레드 == UI 스레드
            mCountTextView.setText("Count : " + mCount);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
```


# 메인 스레드와 스트릭트 모드

안드로이드에서는 크게 세 가지 사항을 메인 스레드 사용 위반 사례로 보는데, 이를 스트릭트 위반이라고 부른다.

```java
안드로이드 스트릭트 모드 3가지

1. 디스크에 파일 쓰기
2. 디스크에서 파일 읽기
3. 네트워크 사용
```

특히 네트워크 사용은 네트워크 상태와 서버의 사장에 따라 소모되는 시간을 유추하기 힘들고, 
시간이 많이 소모될 확률이 크다. 

<b>그러므로 API 10부터는 기본으로 네트워크 사용을 강제적으로 막고 있다.</b>

# 작업 스레드의 필요성

앞서 ANR과 스트릭트 모드를 살펴보면서 작업 스레드의 필요성을 충분히 느꼈을 것이다. 

그렇다면 긴 시간이 소모되는 코드를 메인 스레드가 아닌 <b>작업 스레드</b>에서 실행해 보자.

```java
// 10초 동안 1부터 10까지 증가하는 작업 스레드
Thread workerThread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 10; i++) {
                    mCount++;

                    Log.i(TAG, "Current Count : " + mCount);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        workerThread.start();
}
```
```java
 // 현재까지 카운트한 수치를 텍스트 뷰에 출력한다.
public void onClick(View view) {
        mCountTextView.setText("Count : " + mCount);
    }
```

# UI 업데이트를 할 수 없는 작업 스레드
위의 예제 같은 경우 숫자는 매 초마다 1씩 증가하는데, 그 숫자를 출력하는 것은 버튼을 클릭할 때만 출력한다.

그렇다면 숫자가 증가할 때마다 텍스트 뷰를 업데이트 하기 위해서 작업 스레드 내부에서 업데이트 하면 될 것 같다.

```java
// 10초 동안 1부터 10까지 증가하는 작업 스레드
Thread workerThread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 10; i++) {
                    mCount++;

                    Log.i(TAG, "Current Count : " + mCount);
                    // 과연 작업 스레드에서 UI를 업데이트 할 수 있을까?
                     mCountTextView.setText("Count : " + mCount);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        workerThread.start();
```

위의 코드를 실행하면 다음과 같은 에러가 발생한다.

```java
CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
```
어째서 에러가 발생할까?

안드로이드에서는 <b>UI의 업데이트를 반.드.시 메인 스레드</b>에서 진행하게 되어있다. 
여러 스레드에서 뷰를 변경하여 화면을 갱신한다면 동기화 문제가 발생할 수 있기 때문이다.

즉, 동시에 실행되는 스레드는 어느 것이 더 빨리 처리될지 순서를 알 수 없고, 화면에 뷰를 그리는 작업은 순서가 매우 중요하다. 그러므로 처리되는 순서가 불규칙한 스레드에서 그리는 작업을 하면 화면이 뒤죽박죽 될 수 있다.  따라서 안드로이드에서는 메인 스레드에서만 그리는 작업을 허용하여 그리는 순서를 보장하며, 이를 단일 스레드 GUI 모델이라 한다. 

즉, 단일 스레드 GUI는 하나의 스레드에서만 그리는 작업을 처리하는 것을 말한다. 

그렇다면 작업 스레드에서 UI를 업데이트 해야 하는 일이 생기면 어떻게 해야 할까?

다음의 소스를 살펴보자.

```java
Handler mHandler = new Handler();

Thread workerThread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    mCount++;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Current Count : " + mCount);
                            mCountTextView.setText("Count : " + mCount);
                        }
                    });


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
```

위의 소스에서는 Handler라는 객체를 생성하여 멤버 변수로 선언하고, 작업 스레드 내에서 Handler의 post 함수를 통해 Runnable 객체를 전달하고 있다. 

Runnable 객체는 run 함수를 구현하고 있으며, run 함수에는 텍스트 뷰를 그리는 UI 작업을 처리한다.

결국 작업 스레드에서는 화면을 그릴 수 없기 때문에 Handler 객체를 이용하여 메인 스레드로 그리는 작업을 이관한다. 

Handler에 대해 이해하려면 메인 스레드의 구조를 알고 있어야 한다. 

# 안드로이드 메인 스레드 구조

메인 스레드에는 루퍼와 메시지 큐 객체가 내부적으로 있다. 이 두 객체는 작업 스레드에서 메인 스레드로 GUI 작업을 전달하기 위한 핵심 객체이기도 하다. 또한 이 구조는 안드로이드 앱을 개발하기 위해 반드시 알아야 하는 부분이다.

## 루퍼와 메시지 큐
스레드는 run 함수를 하나 가지며, run 함수 처리가 완료되면 스레드의 생명주기도 끝난다. 

그렇다면 메인 스레드의 run 함수는 어떤 구조이기에 종료되지 않고 계속 유지되는 걸까?

그것은 바로 루퍼라는 객체 때문이다.

```java:Looper.java
public class Looper {
    ...
    public static void loop() {
        ...
        for(;;) {
            ...
        }
    }

}
```

```java
1. 앱이 실행되면 하나의 프로세스가 생성된다.
2. 프로세스는 최소 하나 이상의 스레드를 가진다. 그리고 프로세스가 최초 생성하는 것이 바로 메인 스레드다. 
3. 스레드는 run 함수로 동작을 시작한다.
4. 메인 스레드의 run 함수에서는 루퍼 객체를 생성하고 loop 함수를 실행하여 무한 반복을 시작한다.
```

Looper 클래스 내부에 loop이라는 함수가 있다. loop 함수는 for 문을 이용하여 무한 반복된다. 

즉, 메인 스레드는 앱이 실행되면 생성되고, 메인 스레드 안에서는 Looper 객체를 생성하여 loop 함수를 통해 종료되지 않고 무한 반복된다.

다음은 메세지 큐에 대해 알아보자.

```java:Looper.java
public class Looper {
    private Looper(boolean quitAllowed) {
        mQueue = new MessageQueue(quitAllowed);
        mRun = true;
        mThread = Thread.currentThread();
    }
    ...
    public static void loop() {
        final MessageQueue queue = me.mQueue;
        ...
        for(;;) {
            Message msg = queue.next();
            ...
            msg.target.dispatchMessage(msg);
            ...
        }
    }

}
```
루퍼는 아무런 의미도 없이 반복하고 있는 것이 아니다. 소스를 살펴보면 루퍼가 생성되면서 메시지 큐도 같이 생성되고, 매 반복마다 메시지 큐에서 하나의 메시지를 꺼내서 무언가 처리를 하고 있다.

```java
1. 가장 먼저 메인 스레드가 생성 및 실행된다.
2. 실행을 시작한 메인 스레드는 가장 먼저 루퍼를 생성한다.
3. 생성된 루퍼 객체는 내부적으로 메시지 큐 하나를 생성한다.
4. 루퍼는 무한 반복을 시작하고 매 반복마다 메시지 큐에서 하나의 메세지를 추출한다.
5. 루퍼는 추출된 메시지를 처리하게 된다.
```

정리하자면 루퍼가 생성되면 메시지 큐 하나를 가지게 되고, 매 반복마다 메시지 큐에서 메시지를 추출하여 처리한다. 여기서 메시지는 하나의 실행 작업 단위이며, 해당 작업은 메인 스레드의 실행 흐름인 루퍼가 처리한다. 그러므로 다른 작업 스레드에서 메시지 큐에 메시지를 추가하기만 하면 메인 스레드의 루퍼가 해당 메시지를 처리해 줄 것이다.

다음 절에서는 메시지가 무엇이며, 어떻게 메시지를 메시지 큐에 집어넣는지 알아보자.

## 핸들러

핸들러는 루퍼가 메시지를 처리할 수 있도록 메시지 큐에 메시지를 추가하는 클래스다. 핸들러를 사용해 보기 전에 잠시 루퍼 클래스 소스를 살펴보자.

```java:Looper.java
public class Looper {
    ...
    private static Looper sMainLooper;
    final MessageQueue mQueue;
    ...
    public static Looper getMainLooper() {
        synchronized (Looper.class) {
            return sMainLooper;
        }
    }
}
```

루퍼는 내부적으로 static 키워드 메인 Looper 객체를 가지고 있다. 이 말은 메인 스레드에서 사용하는 루퍼를 정적으로 선언했기 때문에 어디서든 참조할 수 있다는 것이다. 또한 루퍼는 내부에 메시지 큐를 가지고 있다. 그러므로 언제든지 정적으로 선언된 루퍼를 통해 메시지 큐에 메시지를 추가할 수 있다.

물론 sMainLooper는 private로 선언되어 있으므로 getMainLooper라는 함수를 통해 메인 스레드의 루퍼를 참조할 수 있다. 그렇다면 어떻게 메인 스레드의 큐에 메시지를 추가하는지 살펴보자.

안드로이드에서는 루퍼를 제어하기 위한 핸들러 객체를 제공하고 있다. 단순히 어떤 스레드에서라도 핸들러 객체만 생성하면, 핸들러 객체 내부적으로 루퍼 객체를 참조할 수 있게 된다.

```java
1. 작업 스레드에서 Handler 객체를 생성한다. 참고로 꼭 작업 스레드가 아니더라도 상관없다.
2. 생성된 Handler 객체는 메인 스레드에서 생성된 루퍼 객체를 참조하고 있다.
3. 작업 스레드에서는 메시지를 하나 생성하고 처리해야할 실행 코드를 담는다.
4. 생성된 메시지는 Handler 객체를 이용하여 메인 스레드의 메시지 큐에 추가할 수 있다. 
5. 물론 메시지 큐에 메시지를 추가하는 것은 메인 루퍼 객체를 통해 가능하다.
6. 메인 스레드의 루퍼는 메시지 큐에서 메시지를 하나씩 추출하여 처리하게 된다.
```

해당 소스는 다음과 같다.

```java
 // 1. 메시지 큐에 메시지를 추가하기 위한 핸들러를 생성한다.
    Handler mHandler = new Handler();
    
    // 2. 10초 동안 1초에 1씩 카운트 하는 스레드 생성 및 시작
        Thread workerThread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    mCount++;

                    // 3. 실행 코드가 담긴 Runnable 객체를 하나 생성한다.
                    Runnable callback = new Runnable() {
                        @Override
                        public void run() {
                            // 현재까지 카운트한 수치를 텍스트 뷰에 출력한다.
                            mCountTextView.setText("Count : " + mCount);
                        }
                    };

                    // 4. 메시지 큐에 담을 메시지 하나를 생성한다. 생성 시 Runnable 객체를 생성자로 전달한다.
                    Message message = Message.obtain(mHandler, callback);

                    // 5. 핸들러를 통해 메시지를 메시지 큐로 보낸다.
                    mHandler.sendMessage(message);


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        workerThread.start();
```

1. 먼저 액티비티 클래스에서 멤버 변수로 핸들러 객체를 생성한다.
2. 작업 스레드를 생성 및 실행한다.
3. 작업 스레드의 run 함수에서 화면에 그리는 실행 코드를 객체화한 Runnable을 생성한다.
4. 메시지 객체의 obtain 함수를 통해 새로운 메시지 객체를 하나 받고, obtain 함수의 인자를 통해 핸들러와 Runnable 객체를 전달한다.
5. 마지막으로 핸들러 객체의 sendMessage 함수를 통해 메시지를 메시지 큐에 추가하게 된다.


```java
메세지 큐에 메세지를 전달하는 방법

1. 메세지를 생성하지 않고 내부적으로 메시지를 생성하고 바로 메세지 큐에 전달하기
public final boolean post(Runnable r)

2. 메세지를 생성하여 전달하기
public final boolean sendMessage(Message msg)

3. Runnable 객체를 사용하지 않고 handleMessage 함수로 재정의하기
@override public void handleMessage( Message msg )
```

```java:1. 메세지를 생성하지 않고 내부적으로 메세지를 생성하고 바로 메세지 큐에 전달하기 -> Handler.post(Runnable r)
mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Current Count : " + mCount);
                            mCountTextView.setText("Count : " + mCount);
                        }
                    });
```

```java:2. 메세지를 생성하여 전달하기 -> Handler.sendMessage
 Runnable callback = new Runnable() {
                        @Override
                        public void run() {
                            mCountTextView.setText("Count : " + mCount);
                        }
                    };
                    // 메시지 큐에 담을 메시지 하나를 생성한다.
                    // 생성 시 Runnable 객체를 생성자로 전달한다.
                    Message message = Message.obtain(mHandler, callback);
                    // 핸들러를 통해 메시지를 메시지 큐로 보낸다.
                    mHandler.sendMessage(message);
```

```java:3. Runnable 객체를 사용하지 않고 handleMEssage 함수로 재정의하기

     // 해당 메시지가 무엇을 처리하는지 구분하기 위한 상수
    static final private int MESSAGE_DRAW_CONTENT_COUNT = 1;
    
    // 1. 메시지 큐에 메시지를 추가하기 위한 핸들러를 생성한다.
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_DRAW_CONTENT_COUNT: {
                    int currentCount = msg.arg1;
                    TextView countTextView = (TextView)msg.obj;

                    countTextView.setText("Count : " + currentCount);
                    break;
                }
            }
        }
    };
    
    // 10초 동안 1초에 1씩 카운트하는 스레드 생성 및 시작
        Thread workerThread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    mCount++;

                    // 2. 메시지 큐에 담을 메시지를 하나 생성한다.
                    Message message = Message.obtain(mHandler);

                    // 3. 핸들러의 handleMessage로 전달할 값들을 설정한다.

                    // 무엇을 실행하는 메시지인지 구분하기 위해 구분자 설정
                    message.what = MESSAGE_DRAW_CONTENT_COUNT;
                    // 메시지가 실행될 때 참조하는 int형 데이터 설정
                    message.arg1 = mCount;
                    // 메시지가 실행될 때 참조하는 Object형 데이터 설정
                    message.obj = mCountTextView;

                    // 핸들러를 통해 메시지를 메시지 큐로 보낸다.
                    mHandler.sendMessage(message);


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        workerThread.start();
        
1. 핸들러를 생성할 때 handleMessage 함수를 재정의한다. 해당 함수에는 카운트 수를 출력하는 코드를 추가한다.
2. 작업 스레드에서 메시지를 하나 생성한다.
3. 메시지 멤버 변수 what, arg1, obj를 설정한다. what는 해당 메시지가 무엇을 처리하는지 구분하기 위한 상수다.
4. 따라서 handleMessage 함수에서 해당 값을 통해 원하는 처리를 한다. 
5. 정수형 arg1과 오브젝트 타입의 obj는 handleMessage 함수로 전달하는 매개변수와 같다.

```

정리하자면 메시지 객체에 설정되는 실행 코드를 Runnable 객체를 이용하지 않고, 핸들러 클래스 재정의 함수 handleMessage를 사용할 수 있다. 

### 메시지를 처리하는 루퍼
루퍼는 메시지 큐에서 메시지를 하나 추출하고, 메시지에 Runnable 객체가 존재하면 run 함수를 실행한다.

만일 Runnable 객체가 존재하지 않으면 Handler 객체의 handleMessage 함수를 호출한다.

따라서 메시지에 어떤 객체를 구현하여 전달할지 결정하여 사용하면 된다. 

참고로 Runnable 객체는 실행 코드를 추가하는 목적으로 사용되며, Handler의 handleMessage 구현은 메시지의 what 멤버 변수로 구분하여 여러 가지 목적의 실행 코드를 한곳에서 모아 처리할 때 사용한다.

<b>만일 handleMessage가 없다면 처리하려는 실행 코드마다 Runnable 객체를 생성하여 전달해야 할 것이다.</b>

또한 handleMessage 함수의 경우 메시지 객체를 통해 다양한 자료형을 전달할 수 있기 때문에 더 유연하다.

## 루퍼 스케쥴링
지금까지는 하나의 메시지를 생성하고 핸들러를 통해 생성된 메시지를 큐에 추가할 수 있었다. 또한 루퍼는 메시지 큐에 존재하는 메시지를 하나씩 꺼내 즉시 처리해 주었다. 

그렇다면 큐에 추가된 메시지가 루퍼에 의해 즉시 처리되지 않고 원하는 시간에 실행될 수는 없을까?

메시지의 간단한 설정만으로 원하는 시간에 메시지가 처리되도록 하는 루퍼 스케쥴링이 가능하다.

다음의 소스를 살펴보자.

```java
Thread workerThread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    ...
                    mHandler.sendMessageDelayed(message, 10000);
                    ...
                }
            }
        };

        workerThread.start();
```

### 메시지를 생성하는 함수
메시지 생성은 메시지의 obtain 함수를 통해 가능하다.

* Message.obtain()
  : 빈 메시지 객체를 얻어온다.
* Message.obtain(Message orig)
: 인자로 전달한 orig 메시지 객체를 복사한 새로운 메시지 객체를 얻어온다.
* Message.obtain(Handler h)
  : 인자로 전달한 핸들러 객체가 설정된 메시지 객체를 얻어온다.
* Message.obtain(Handler h, Runnable callback) 
    : 인자로 전달한 핸들러, Runnable 객체가 설정된 메시지 객체를 얻어온다.
* Message.obtain(Handler h, int what)
 : 인자로 전달한 핸들러, what 객체가 설정된 메시지 객체를 얻어온다.
* Message.obtain(Handler h, int what, Object obj)
 : 인자로 전달한 핸들러, what, obj 객체가 설정된 메시지 객체를 얻어온다.
* Message.obtain(Handler h, int what, int arg1, int arg2)
* Message.obtain(Handler h, int what, int arg1, int arg2, Object obj)

# 안드로이드 Thread & AsyncTask
안드로이드에서 Thread는 다음의 2 종류로 나눌 수 있다.

* UI Thread = Foreground Thread = Main Thread
    => 보이는 부분에서 동작하는 Thread. UI 업데이트는 이곳에서만 가능하다.
    <br>
* Thread = Background Thread = Worker Thread
=> 보이지 않는 부분에서 동작하는 Thread. UI의 업데이트가 불가능하다.

네트워크 연결이나 파일 다운로드 등의 시간이 오래걸릴 수 있는 작업을 Main Thread에서 실행하면 OS 단에서 ANR(Android Not Responding) 에러를 발생시킨다.

위의 Thread와 Handler를 이용한 방법이 불편해서 안드로이드에서 제공하는 Helper Class가 바로 AsyncTask 이다.

# UI Thread
UI Thread의 종류는 크게 2 가지이다.

## 1. Handler    : Java 제공 클래스

```java
Handler handler = new Handler() {
    @override
    public void handleMessage(Message msg) {
        TextView.setText("" + msg.arg1);
    }
}
```

## 2. runOnUiThread ()   : 안드로이드 Activity 제공 메소드

백그라운드 처리는 바로바로 보이지만, UI 변경은 Thread 종료 시 마지막 결과만 보여진다.
```java
 runOnUiThread(new Runnable() {
            @Override 
            public void run() { 
                // 스레드로 동작 하는 부분 
                for (int i = 0; i < 10; i++) {
                    try { 
                        Thread.sleep(1000); // 스레드가 잠시 쉰다 1초
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // 에러 처리 
                    } 
                    Log.d(TAG, "" + i); // background
                    mNumberTextView1.setText("" + i); // foreground
                } 
            } 
        }); 
```

# Background Thread
UI의 변경은 이곳에서 할 수 없다.
## 1. Worker Thread

```java
  Thread thread = new Thread(new Runnable() {
            @Override 
            public void run() { 
                // 스레드로 동작 하는 부분 
                for (int i = 0; i < 10; i++) {
                    try { 
                        Thread.sleep(1000); // 스레드가 잠시 쉰다 1초
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // 에러 처리 
 
 
                    } 
                    Log.d(TAG, "" + i);
                } 
            } 
        }); 
        thread.start();
```

```java
new Thread(new Runnable() {
            @Override 
            public void run() { 
                // 2초 동안 다운로드 
                for (int i = 0; i < 10; i++) {
                    try { 
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } 
                } 
 
 
                // 다운로드 끝나면 progressDialog를 닫는다 
                progressDialog.dismiss();
            } 
        }).start();
```

# AsyncTask
위의 Thread와 Handler가 불편하여 안드로이드에서 제공해주는 Helper Class이다.
## AsyncTask의 선언과 관련 메소드
## 1. 선언
```java
private class DownloadTask extends AsyncTask<Void, Integer, Void> {
    .......
}
```
AsyncTask를 상속하여 구현하는데, 옆의 제네릭 타입은 내부 메소드에서 사용되는 매개변수나 리턴 타입을 결정한다.

## 2. 관련 메소드
주로 사용되는 메소드는 다음과 같다.

* onPreExecute() : UI Thread, doInBackground() 전에 호출된다.
```java
// UI Thread 
        // doInBackground 전에 호출 됨 
        @Override 
        protected void onPreExecute() { 
            super.onPreExecute(); 
            ...         
        } 
```
* doInBackground() : Background Thread, onProgressUpdate() 를 publishProgress() 로 호출할 수 있다.
```java
// Background 쓰레드 
        @Override 
        protected Void doInBackground(Void... params) {
            // 다운로드 처리 
            for (int i = 0; i < 100; i++) {
                // 0.2초 쉬고 
                try { 
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                } 
 
 
                // onProgressUpdate를 호출 
                publishProgress(i + 1);
            } 
            return null; 
        } 
```
* publishProgress() : doInBackground() 에서 onProgressUpdate() 를 호출할 때 사용한다.
```java
```
* onProgressUpdate() : UI Thread, publishProgress()로 호출되어 실행된다.
```java
 // UI Thread 
        // doInBackground 에서 publishProgress 로 호출하면 호출 됨 
        // 직접 호출 하지 않는 이유 : 죽으니까 
        // http://developer.android.com/intl/ko/reference/android/os/AsyncTask.html 
        @Override 
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
 
 
            mProgressBar.setProgress(values[0]);
            mNumberTextView2.setText(values[0] + "%");
        } 
```
* onPostExecute() : UI Thread, doInBackground 가 수행된 후에 호출된다.
```java
// UI Thread 
        // doInBackground 가 수행 된 후에 호출 됨 
        // doInBackground 에서 return 된 값이 파라메터로 넘어 옴 
        @Override 
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
 
 
            mmBuilder.show();
        } 
```

## 3. Activity 또는 Fragment에서의 사용법
```java
new DownloagTask.execute();
```

execute() 메소드의 파라메터의 타입은 첫번째 제네릭 타입이 결정한다.
```java

// 예를 들어 첫번째 제네릭 타입이 Integer 인 경우,
... extends AsyncTask<Integer, Void, Void>

//AsyncTask를 실행할 때, 매개변수로 Integer 타입의 데이터를 1개, 또는 여러개 넣을 수 있다.
// 이것을 Java 에서는 Variable Arguments 라고 한다.
AsyncTask.execute(1, 2, 3, 4);

// 이렇게 들어온 매개변수들은 그 수가 단수이든, 복수던간에 배열로 내부에서 치환된다.
AsyncTask.doInBackground(Integer... params) {
    ...
    // 매개변수의 개수인 4를 출력한다.
    Log.d(TAG, params.size);
    // 배열의 0번째 인덱스에 위치한 1을 출력한다.
    Log.d(TAG, params[0]);
    ...
    
}

```

execute() 메소드의 파라메터는 AsyncTask 클래스 내부 doInBackground()의 파라메터로 전달된다.

## 4. AsyncTask<T, T, T> 의 세가지 제네릭 타입

### 1. 첫번째 제네릭 타입

위에서 정리한 바와 같이, AsyncTask<T, T, T> 의 첫번째 타입은 doInBackground(T... params) 메소드의 타입이다.

### 2. 두번째 제네릭 타입
```java

// 다음으론 AsyncTask의 두번째 제네릭 타입이다.
// 이것은 onProgressUpdate()와 이것을 호출하기 위한 publishProgress()의 파라메터 타입이 된다.
... extends AsyncTask<Void, Integer, Void>

@Override
protected Void doInBackground(Void... params) {
    for (int i = 0; i < 100; i++) {
                // 0.2초 쉬고 
                try { 
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                } 
 
 
                // onProgressUpdate를 호출 
                // 이곳을 자세히 보면, publishProgress()로 Integer 타입의 데이터를 넘겨주고 있음을 알 수 있다.
                publishProgress(i + 1);
            } 
            return null; 
}

// 다음은 doInBackground() 내부의 publishProgress() 로 부터 데이터를 넘겨받아 UI 처리를 하는 onProgressUpdate()

@Override 
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
 
 
            mProgressBar.setProgress(values[0]);
            mNumberTextView2.setText(values[0] + "%");
        } 
```

### 3. 세번째 제네릭 타입

```java
// 다음으론 AsyncTask의 세번째 제네릭 타입이다.
// 이것은 doInBackground()의 리턴 타입과, onPostExecute()의 파라메터 타입을 결정한다.
... extends AsyncTask<Void, Void, Integer>

// 무언가 작업을 하고 정수 100을 리턴하는 doInBackground()의 모습이다.
@Override
protected Integer doInBackground(Void... params) {
    ...
    int result = 100;
    return result;
}

// 여기서 리턴된 데이터는 doInBackground()의 종료 이후에 실행되는 onPostExecute()로 들어간다.
// onPostExecute()는 하나의 값 만을 받을 수 있다.

@Override 
        protected void onPostExecute(Integer param) {
            super.onPostExecute(aVoid);
            ...
        } 

```


## 전체적인 모습

```java

private class DownloadTask extends AsyncTask<Void, Integer, Void> {
        private AlertDialog.Builder mmBuilder;
 
 
        // UI Thread 
        // doInBackground 전에 호출 됨 
        @Override 
        protected void onPreExecute() { 
            super.onPreExecute(); 
 
 
            mmBuilder = new AlertDialog.Builder(ThreadActivity.this);
            mmBuilder.setMessage("다운로드가 완료 되었습니다");
            mmBuilder.setNegativeButton("닫기", null);
 
 
            mProgressBar.setProgress(0);
        } 
 
 
        // Background 쓰레드 
        @Override 
        protected Void doInBackground(Void... params) {
            // 다운로드 처리 
            for (int i = 0; i < 100; i++) {
                // 0.2초 쉬고 
                try { 
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                } 
 
 
                // onProgressUpdate를 호출 
                publishProgress(i + 1);
            } 
            return null; 
        } 
 
 
        // UI Thread 
        // doInBackground 에서 publishProgress 로 호출하면 호출 됨 
        // 직접 호출 하지 않는 이유 : 죽으니까 
        // http://developer.android.com/intl/ko/reference/android/os/AsyncTask.html 
        @Override 
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
 
 
            mProgressBar.setProgress(values[0]);
            mNumberTextView2.setText(values[0] + "%");
        } 
 
 
        // UI Thread 
        // doInBackground 가 수행 된 후에 호출 됨 
        // doInBackground 에서 return 된 값이 파라메터로 넘어 옴 
        @Override 
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
 
 
            mmBuilder.show();
        } 
    } 
```
    






 
