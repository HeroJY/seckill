/**
 * 存放主要交互逻辑js代码
 * javascript 模块化
 * 
 */
var seckill={
	//封装秒杀Ajax的地址
	URL:{
		now:function(){
			return '/seckill/time/now';
		},
		exposer:function(seckillId){
			return '/seckill/'+seckillId+'/exposer';
		},
		execution:function(seckillId,md5){
			return '/seckill/' + seckillId + '/' + md5 + '/execution';
		}
	},
	
	//处理秒杀逻辑
	handlerSeckill:function(seckillId,node){
		//获取秒杀地址，控制显示逻辑，执行秒杀
		node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');//按钮
		$.post(seckill.URL.exposer(seckillId),{},function(result){
			//在回调函数中执行交互流程
			if(result && result['success']){
				var exposer = result['data'];
				if(exposer['exposed']){
					//开启秒杀
					//获取秒杀地址
					var md5 = exposer['md5'];
					var killUrl = seckill.URL.execution(seckillId, md5);
//					console.log(killUrl);
					//one表示只绑定一次click事件，防止用户连续点击
					$('#killBtn').one('click',function(){
						//执行秒杀请求
						//this表明在哪个对象里面执行，就获取哪个对象
						//1.先禁用按钮
						$(this).addClass('disable');
						//2.发送秒杀请求
						$.post(killUrl,{},function(result){
							if(result){
								if(result['success']){
									var killResult = result['data'];
									var stateInfo = killResult['stateInfo'];
									//显示秒杀结果
									node.html('<sapn class="label label-success">' + stateInfo + '</span>');
								}else{
									//未注册或者程序内部异常
									var error = result['error'];
									node.html('<sapn class="label label-danger">' + error + '</span>');
									if(!seckill.validatePhone($.cookie('killphone'))){
										var killPhoneModal = $('#killPhoneModal');
										killPhoneModal.modal({
											show:true,//显示弹出层
											backdrop:'static',//禁止位置关闭
											keyboard:false//关闭键盘事件
										});
										$('#killPhoneBtn').click(function(){
											var phone = $('#killPhoneKey').val();
											if(seckill.validatePhone(phone)){
												$.cookie('killPhone',phone,{expires:1,path:'/seckill'});
												window.location.reload();
											}else{
												$('#killPhoneMessage').hide().html('<label class="label label-danger">手机号出错！</label>').show(300);
											};
										});
									}
								}
							}
						});
					});
					node.show();
				}else{
					//未开启秒杀,可能自己电脑时间变化和服务器时间变化不一致
					var now = exposer['now'];
					var start = exposer['start'];
					var end = exposer['end'];
					//重新进入计时逻辑
					seckill.countDown(seckillId, now, start, end);
				}
			}else{
				console.log(result);
			}
		});
	},
	
	//验证手机号
	validatePhone:function(phone){
		//判断这个手机号是否为空和是否是11位以及是否含有字母
		if(phone && phone.length == 11 && !isNaN(phone)){
			return true;
		}else{
			return false;
		}
	},
	
	//判断时间
	countDown:function(seckillId,nowTime,startTime,endTime){
		var seckillBox = $('#seckill-box');
		//时间的判断
		if(nowTime > endTime){
			//秒杀结束
			seckillBox.html('秒杀结束！');
		}else if(nowTime < startTime){
			//秒杀未开始,计时事件绑定
			var killTime = new Date(startTime + 1000);//1000是毫秒
			
			seckillBox.countdown(killTime,function(event){
				//控制时间格式
				//每一次的时间变化都会调用这个函数
				var format = event.strftime('秒杀倒计时： %D天 %H时 %M分 %S秒');
				seckillBox.html(format);
				//时间完成后回调事件
			}).on('finish.countdown',function(){
				//获取秒杀地址，控制显示逻辑，执行秒杀
				seckill.handlerSeckill(seckillId,seckillBox);
			});
		}else{
			//秒杀开始
			seckill.handlerSeckill(seckillId,seckillBox);
		}
	},
	
	//详情页秒杀逻辑
	detail:{
		//详情页初始化
		init:function(params){
			//手机验证和登录，计时交互
			//规划我们的交互流程
			//在cookie中查找手机号
			var killPhone = $.cookie('killPhone');
			//验证手机号
			if(!seckill.validatePhone(killPhone)){
				//绑定phone
				var killPhoneModal = $('#killPhoneModal');
				//显示弹出层
				killPhoneModal.modal({
					show:true,//显示弹出层
					backdrop:'static',//禁止位置关闭
					keyboard:false//关闭键盘事件
				});
				$('#killPhoneBtn').click(function(){
					var inputPhone = $('#killPhoneKey').val();
					console.log(inputPhone);
					if(seckill.validatePhone(inputPhone)){
						//电话写入cookie
						$.cookie('killPhone',inputPhone,{expires:1,path:'/seckill'});
						//刷新页面
						window.location.reload();
					}else{
						$('#killPhoneMessage').hide().html('<label class="label label-danger">手机号出错！</label>').show(300);
					};
				});
			}
			//已经登录
			//计时交互
			var startTime = params['startTime'];
			var endTime = params['endTime'];
			var seckillId = params['seckillId'];
			$.get(seckill.URL.now(),{},function(result){
				if(result && result['success']){
					var nowTime = result['data'];
					//时间判断,计时交互
					seckill.countDown(seckillId, nowTime, startTime, endTime);
				}else{
					console.log(result);
				};
			});
		}
	}	
};